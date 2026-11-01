package com.sport.service.services.eventimport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KassirEventParser {
    public static final String LIST_URL =
            "https://vrn.kassir.ru/bilety-na-sportivnye-meropriyatiya";
    private final ObjectMapper mapper;

    public KassirEventParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<EventCandidate> listing(String html) {
        Document document = Jsoup.parse(html, LIST_URL);
        Map<String, EventCandidate> cards = new LinkedHashMap<>();
        for (Element card : document.select("article.compilation-tile")) {
            Element title = card.selectFirst("h2");
            Element anchor = card.selectFirst("a[href*=/sport/]");
            if (anchor == null) continue;
            String url = anchor.absUrl("href");
            if (!validUrl(url)) continue;
            EventCandidate candidate =
                    new EventCandidate(EventCandidate.Source.KASSIR, "page:" + url, url);
            candidate.setName(title == null ? anchor.text() : title.text());
            Element venue = card.selectFirst(".compilation-tile__venue");
            if (venue != null) candidate.setPlaceName(venue.text());
            Element date = card.selectFirst("time");
            if (date != null)
                candidate.getCollected().put("Дата в списке (может быть диапазоном)", date.text());
            cards.putIfAbsent(url, candidate);
        }
        if (cards.isEmpty() && !document.text().matches("(?s).*Найдено\\s+0\\s+событи.*")) {
            throw new IllegalArgumentException(
                    "Не найден список спортивных событий Kassir: возможно, изменилась страница");
        }
        return new ArrayList<>(cards.values());
    }

    public List<EventCandidate> detail(String html, EventCandidate fallback) {
        Document doc = Jsoup.parse(html, fallback.getLink());
        Element script = doc.getElementById("__NUXT_DATA__");
        Map<String, EventCandidate> events = new LinkedHashMap<>();
        try {
            if (script == null)
                throw new IllegalArgumentException("Не найдены данные сеансов Kassir");
            JsonNode table = mapper.readTree(script.data());
            String description = null;
            for (JsonNode node : table) {
                if (node.isObject()
                        && node.has("description")
                        && node.has("url")
                        && samePage(value(table, node, "url"), fallback.getLink())) {
                    String text = value(table, node, "description");
                    if (!EventCandidate.blank(text)) description = text;
                }
            }
            for (JsonNode node : table) {
                if (!node.isObject()
                        || !node.has("id")
                        || !node.has("beginsAt")
                        || !node.has("title")
                        || !node.has("url")) continue;
                String url = value(table, node, "url");
                if (!samePage(url, fallback.getLink())) continue;
                String id = value(table, node, "id");
                EventCandidate event = new EventCandidate(EventCandidate.Source.KASSIR, id, url);
                event.setName(value(table, node, "title"));
                String ownDescription = value(table, node, "description");
                event.setDescription(
                        EventCandidate.blank(ownDescription) ? description : ownDescription);
                JsonNode venue = ref(table, node.get("venue"));
                event.setPlaceName(value(table, venue, "name"));
                JsonNode address = ref(table, venue.get("address"));
                event.setAddress(value(table, address, "addressString"));
                String city = value(table, address, "city");
                if (!EventCandidate.blank(city)
                        && !"воронеж".equals(EventImportRules.normalize(city))) continue;
                String rawStart = value(table, node, "beginsAt");
                event.getCollected().put("Начало в источнике", rawStart);
                try {
                    // Kassir serializes the displayed local wall time with +00:00. Do not add three
                    // hours.
                    var start = OffsetDateTime.parse(rawStart).toLocalDateTime();
                    event.setDate(start.toLocalDate());
                    event.setTime(start.toLocalTime());
                } catch (RuntimeException e) {
                    event.getProblems().add("Не удалось разобрать дату/время сеанса");
                }
                EventImportRules.apply(event);
                EventCandidate old = events.get(id);
                if (old == null || old.missingFields().size() > event.missingFields().size())
                    events.put(id, event);
            }
            if (events.isEmpty())
                throw new IllegalArgumentException("Не найдены отдельные сеансы события");
        } catch (Exception e) {
            Element title = doc.selectFirst("h1");
            if (title != null) fallback.setName(title.text());
            fallback.getProblems().add("Не удалось разобрать карточку: " + e.getMessage());
            fallback.getCollected().put("Текст карточки", doc.body().text());
            EventImportRules.apply(fallback);
            return List.of(fallback);
        }
        return new ArrayList<>(events.values());
    }

    private JsonNode ref(JsonNode table, JsonNode index) {
        if (index == null || !index.isInt() || index.asInt() < 0) return mapper.nullNode();
        JsonNode value = table.path(index.asInt());
        return value.isMissingNode() ? mapper.nullNode() : value;
    }

    private String value(JsonNode table, JsonNode node, String name) {
        JsonNode value = ref(table, node.get(name));
        return value.isValueNode() && !value.isNull() ? value.asText() : null;
    }

    static boolean validUrl(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equals(uri.getScheme())
                    && "vrn.kassir.ru".equals(uri.getHost())
                    && uri.getPath().startsWith("/sport/");
        } catch (RuntimeException e) {
            return false;
        }
    }

    static boolean samePage(String one, String two) {
        return one != null && two != null && one.split("#", 2)[0].equals(two.split("#", 2)[0]);
    }
}
