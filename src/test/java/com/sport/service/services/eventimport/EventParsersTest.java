package com.sport.service.services.eventimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sport.service.entities.enums.common.District;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

class EventParsersTest {
    private final KassirEventParser kassir = new KassirEventParser(new ObjectMapper());

    @Test
    void extractsThreeDistinctSessionsFromOneRealKassirCardWithoutUtcShift() throws Exception {
        var cards = kassir.listing(fixture("kassir-list.html"));
        assertThat(cards).hasSize(2);
        var events = kassir.detail(fixture("kassir-buran.html"), cards.getFirst());
        assertThat(events).hasSize(3);
        assertThat(events)
                .extracting(EventCandidate::getExternalId)
                .containsExactly("6224472", "6224473", "6224475");
        assertThat(events)
                .extracting(EventCandidate::getTime)
                .containsExactly(LocalTime.of(17, 0), LocalTime.of(17, 0), LocalTime.of(19, 0));
        assertThat(events)
                .extracting(EventCandidate::getDate)
                .containsExactly(
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 4),
                        LocalDate.of(2026, 11, 6));
        assertThat(events).allMatch(EventCandidate::complete);
        assertThat(events).allMatch(event -> event.getDistrict() == District.CENTRALNYY);
        assertThat(events.getFirst().getDescription())
                .isEqualTo("Хоккейный клуб «Буран» г. Воронеж был основан в 1977 году.");
    }

    @Test
    void fakelFiltersAwayGamesAndPreservesUnknownTime() throws Exception {
        var events = new FakelEventParser().parse(fixture("fakel.html"), LocalDate.of(2026, 11, 1));
        assertThat(events)
                .extracting(EventCandidate::getExternalId)
                .containsExactly("2991", "2997", "2999", "3003");
        var next = events.get(1);
        assertThat(next.getName()).isEqualTo("Футбольный матч Факел — Динамо");
        assertThat(next.getDate()).isEqualTo(LocalDate.of(2026, 11, 7));
        assertThat(next.getTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(next.getDescription()).contains("14 тур");
        assertThat(next.getAddress()).isEqualTo("ул. Писателя Маршака, 1г");
        assertThat(next.getDistrict()).isEqualTo(District.SOVETSKYY);
        assertThat(events.getLast().getDate()).isEqualTo(LocalDate.of(2027, 3, 14));
        assertThat(events.getLast().missingFields()).containsExactly("время");
    }

    @Test
    void handlesRelativeDatesAndCalendarSeasonsWithoutGuessingUnknownYear() {
        LocalDate today = LocalDate.of(2026, 12, 31);
        assertThat(FakelEventParser.parseDate("Завтра, 14:00", "2026/2027", today))
                .isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(FakelEventParser.parseDate("02 января", "2026/2027", today))
                .isEqualTo(LocalDate.of(2027, 1, 2));
        assertThat(FakelEventParser.parseDate("02 января", "2026", today))
                .isEqualTo(LocalDate.of(2026, 1, 2));
        assertThatThrownBy(() -> FakelEventParser.parseDate("02 января", "", today))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void firstSentenceRetainsAbbreviationsAndStripsHtml() {
        String first =
                "Бойцовский клуб Victory Team представляет вечер профессионального бокса с участием"
                        + " спортсменов из Воронежа и других регионов.";
        assertThat(
                        EventImportRules.firstSentence(
                                "<p>" + first + "</p><p>Ещё одно предложение.</p>"))
                .isEqualTo(first);
        assertThat(EventImportRules.firstSentence("В г. Воронеж состоится турнир. Далее."))
                .isEqualTo("В г. Воронеж состоится турнир.");
        assertThat(EventImportRules.firstSentence("Дистанция 3.5 км! Дальше."))
                .isEqualTo("Дистанция 3.5 км!");
    }

    @Test
    void assignsCentralDistrictOnlyForKnownVenueOrRevolutionAvenue() {
        for (String address :
                List.of(
                        "проспект Революции, 56",
                        "Воронеж, пр-кт Революции, 56",
                        "пр-т Революции, 56")) {
            var event =
                    new EventCandidate(
                            EventCandidate.Source.KASSIR, "1", "https://vrn.kassir.ru/sport/test");
            event.setAddress(address);
            EventImportRules.apply(event);
            assertThat(event.getDistrict()).isEqualTo(District.CENTRALNYY);
        }
        var aura = new EventCandidate(EventCandidate.Source.KASSIR, "1", "url");
        aura.setPlaceName("Клуб AURA");
        EventImportRules.apply(aura);
        assertThat(aura.getDistrict()).isEqualTo(District.CENTRALNYY);
        var unknown = new EventCandidate(EventCandidate.Source.KASSIR, "2", "url");
        unknown.setAddress("ул. Революции 1905 года, 1");
        EventImportRules.apply(unknown);
        assertThat(unknown.getDistrict()).isNull();
    }

    @Test
    void malformedDetailKeepsCollectedListingFieldsForAdmin() {
        var card =
                new EventCandidate(
                        EventCandidate.Source.KASSIR, "page:1", "https://vrn.kassir.ru/sport/test");
        card.setPlaceName("AURA");
        var event = kassir.detail("<h1>Бокс</h1><p>Время уточняется</p>", card).getFirst();
        assertThat(event.getName()).isEqualTo("Бокс");
        assertThat(event.getPlaceName()).isEqualTo("AURA");
        assertThat(event.complete()).isFalse();
        assertThat(EventImportReport.incomplete(event))
                .contains(
                        "Время уточняется",
                        "Нужно заполнить вручную",
                        "дата",
                        "время",
                        "/create_event");
        assertThatThrownBy(
                        () ->
                                new FakelEventParser()
                                        .parse("<html>Проверка браузера</html>", LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportChunksEscapeTelegramHtmlWithoutDroppingData() {
        String report = "<b>& тест 😀 ".repeat(1000);
        var parts = EventImportReport.telegramParts(report);
        assertThat(parts).allMatch(part -> part.length() <= 3500 && !part.contains("<b>"));
        assertThat(org.springframework.web.util.HtmlUtils.htmlUnescape(String.join("", parts)))
                .isEqualTo(report);
    }

    static String fixture(String name) throws Exception {
        try (var input = EventParsersTest.class.getResourceAsStream("/eventimport/" + name)) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
