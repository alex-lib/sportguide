package com.sport.service.services.eventimport;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class FakelEventParser {
    public static final String LIST_URL = "https://fakelfc.ru/matches";
    private static final List<String> MONTHS =
            List.of(
                    "января",
                    "февраля",
                    "марта",
                    "апреля",
                    "мая",
                    "июня",
                    "июля",
                    "августа",
                    "сентября",
                    "октября",
                    "ноября",
                    "декабря");
    private static final Pattern DATE =
            Pattern.compile("(\\d{1,2})\\s+([а-я]+)(?:\\s+(20\\d{2}))?");
    private static final Pattern TIME = Pattern.compile("(?<!\\d)([0-2]?\\d:[0-5]\\d)(?!\\d)");

    public List<EventCandidate> parse(String html, LocalDate today) {
        Document doc = Jsoup.parse(html, LIST_URL);
        var cards = doc.select(".card--matchRow");
        if (cards.isEmpty()
                && (doc.selectFirst(".grid--matches") == null
                        || doc.selectFirst("select[name=season] option") == null))
            throw new IllegalArgumentException(
                    "Не найден календарь Факела: возможно, проверка браузера или новая разметка");
        Element seasonOption = doc.selectFirst("select[name=season] option[selected]");
        // HTML selects use the first option when no explicit selected attribute is present.
        if (seasonOption == null) seasonOption = doc.selectFirst("select[name=season] option");
        String season = seasonOption == null ? "" : seasonOption.text();
        List<EventCandidate> events = new ArrayList<>();
        for (Element card : cards) {
            var teams = card.select(".matchScoreTeam__title");
            if (!teams.isEmpty()
                    && !"факел".equals(EventImportRules.normalize(teams.getFirst().text())))
                continue;
            Element anchor = card.selectFirst("a[href*=/matches/]");
            String link = anchor == null ? LIST_URL : anchor.absUrl("href");
            var idMatch =
                    Pattern.compile("https://fakelfc\\.ru/matches/(\\d+)(?:[?#].*)?").matcher(link);
            String id = idMatch.matches() ? idMatch.group(1) : null;
            EventCandidate event = new EventCandidate(EventCandidate.Source.FAKEL, id, link);
            if (teams.size() == 2)
                event.setName(
                        "Футбольный матч " + teams.get(0).text() + " — " + teams.get(1).text());
            else
                event.getProblems()
                        .add("Не удалось определить обе команды и подтвердить домашний матч");
            var info = card.select(".card__center .card__info > li");
            String raw = info.isEmpty() ? "" : info.get(0).text();
            event.getCollected().put("Дата и время в календаре", raw);
            event.getCollected().put("Сезон", season);
            if (info.size() > 1) event.setPlaceName(info.get(1).text());
            if (info.size() > 2) event.setDescription(info.get(2).text());
            try {
                event.setDate(parseDate(raw, season, today));
                var time = TIME.matcher(raw);
                if (time.find())
                    event.setTime(
                            LocalTime.parse(
                                    time.group(1).length() == 4
                                            ? "0" + time.group(1)
                                            : time.group(1)));
            } catch (RuntimeException e) {
                event.getProblems().add("Не удалось определить дату с учётом сезона");
            }
            EventImportRules.apply(event);
            events.add(event);
        }
        return events;
    }

    static LocalDate parseDate(String raw, String season, LocalDate today) {
        String text = raw.toLowerCase(Locale.ROOT);
        if (text.contains("сегодня")) return today;
        if (text.contains("завтра")) return today.plusDays(1);
        if (text.contains("вчера")) return today.minusDays(1);
        var match = DATE.matcher(text);
        if (!match.find()) throw new IllegalArgumentException("Дата отсутствует");
        int month = MONTHS.indexOf(match.group(2)) + 1;
        int year;
        if (match.group(3) != null) year = Integer.parseInt(match.group(3));
        else {
            var years = Pattern.compile("^(20\\d{2})(?:/(20\\d{2}))?$").matcher(season.strip());
            if (!years.matches()) throw new IllegalArgumentException("Неизвестен сезон");
            year = Integer.parseInt(years.group(month < 7 && years.group(2) != null ? 2 : 1));
        }
        return LocalDate.of(year, month, Integer.parseInt(match.group(1)));
    }
}
