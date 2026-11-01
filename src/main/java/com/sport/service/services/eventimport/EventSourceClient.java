package com.sport.service.services.eventimport;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EventSourceClient {
    public static final String FAKEL_CUP_URL = FakelEventParser.LIST_URL + "?competition=2";

    Connection newSession() {
        return Jsoup.newSession()
                .timeout(30000)
                .userAgent("Mozilla/5.0 (compatible; SportguideEventImport/1.0)")
                .header("Accept-Language", "ru-RU,ru;q=0.9")
                .maxBodySize(8 * 1024 * 1024);
    }

    public String kassirListing() throws IOException {
        String html = kassir(KassirEventParser.LIST_URL);
        var document = Jsoup.parse(html);
        var count =
                java.util.regex.Pattern.compile("Найдено\\s+(\\d+)\\s+событи")
                        .matcher(document.text());
        if (count.find()
                && Integer.parseInt(count.group(1))
                        > document.select("article.compilation-tile").size()) {
            throw new IOException(
                    "Kassir вернул неполный список событий: требуется проверка пагинации");
        }
        return html;
    }

    public String kassir(String url) throws IOException {
        if (!KassirEventParser.LIST_URL.equals(url) && !KassirEventParser.validUrl(url)) {
            throw new IllegalArgumentException("Недопустимый адрес Kassir");
        }
        return get(newSession(), url.split("#", 2)[0], "vrn.kassir.ru").body();
    }

    public String fakel() throws IOException {
        return get(newSession(), FakelEventParser.LIST_URL, "fakelfc.ru").body();
    }

    public String fakelCup() throws IOException {
        return fakelTournament(2);
    }

    public String fakelTournament(int competition) throws IOException {
        if (!java.util.Set.of(32, 2, 9).contains(competition))
            throw new IllegalArgumentException("Неизвестный турнир Факела");
        // One HTTP session retains the cookies that belong to this page's CSRF token.
        Connection session = newSession();
        String html = get(session, FakelEventParser.LIST_URL, "fakelfc.ru").body();
        var token = Jsoup.parse(html).selectFirst("meta[name=csrf-token]");
        if (token == null || token.attr("content").isBlank()) {
            throw new IOException(
                    "Факел: отсутствует токен фильтра турнира; возможно, сайт вернул страницу"
                            + " проверки");
        }
        var response =
                session.newRequest(FakelEventParser.LIST_URL + "/list")
                        .method(Connection.Method.POST)
                        .referrer(FakelEventParser.LIST_URL)
                        .header("X-CSRF-TOKEN", token.attr("content"))
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Accept", "application/json")
                        .data("competition", Integer.toString(competition))
                        .ignoreContentType(true)
                        .execute();
        checkHost(response, "fakelfc.ru");
        return cupCalendar(response.body());
    }

    static String cupCalendar(String json) throws IOException {
        var result = new ObjectMapper().readTree(json);
        if (result == null
                || !result.path("grid").isTextual()
                || !result.path("select_seasons").isTextual()) {
            throw new IOException("Факел: неожиданный ответ фильтра турнира");
        }
        // Keep the tournament's own season selector: it may differ from the league calendar.
        String html = result.path("select_seasons").asText() + result.path("grid").asText();
        var doc = Jsoup.parse(html);
        if (doc.selectFirst("select[name=season] option") == null
                || doc.selectFirst(".grid--matches") == null) {
            throw new IOException("Факел: в ответе турнира отсутствуют календарь или сезон");
        }
        return html;
    }

    private Connection.Response get(Connection session, String url, String host)
            throws IOException {
        IOException failure = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                var response = session.newRequest(url).execute();
                checkHost(response, host);
                return response;
            } catch (IOException e) {
                failure = e;
            }
        }
        throw failure;
    }

    private void checkHost(Connection.Response response, String host) throws IOException {
        if (!host.equals(response.url().getHost())
                || !"https".equals(response.url().getProtocol())) {
            throw new IOException("Неожиданный адрес ответа источника");
        }
    }
}
