package com.sport.service.services.eventimport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

class EventSourceClientTest {
    @ParameterizedTest
    @ValueSource(ints = {32, 2, 9})
    void tournamentsUsePostWithCompetitionAndFreshCsrfTokenInSameSession(int competition)
            throws Exception {
        var client = spy(new EventSourceClient());
        var session = mock(Connection.class);
        var landing = mock(Connection.class);
        var filter = mock(Connection.class, RETURNS_SELF);
        var landingResponse = mock(Connection.Response.class);
        var cupResponse = mock(Connection.Response.class);
        doReturn(session).when(client).newSession();
        when(session.newRequest(FakelEventParser.LIST_URL)).thenReturn(landing);
        when(session.newRequest(FakelEventParser.LIST_URL + "/list")).thenReturn(filter);
        when(landing.execute()).thenReturn(landingResponse);
        when(landingResponse.url()).thenReturn(URI.create(FakelEventParser.LIST_URL).toURL());
        when(landingResponse.body())
                .thenReturn("<meta name='csrf-token' content='fresh-test-token'>");
        when(filter.execute()).thenReturn(cupResponse);
        when(cupResponse.url()).thenReturn(URI.create(FakelEventParser.LIST_URL + "/list").toURL());
        when(cupResponse.body()).thenReturn(cupFixture());

        var events =
                new FakelEventParser()
                        .parse(client.fakelTournament(competition), LocalDate.of(2026, 11, 1));
        assertThat(events).hasSize(3).allMatch(EventCandidate::complete);
        assertThat(events).allMatch(e -> e.getDescription().startsWith("FONBET Кубок России"));
        assertThat(events)
                .extracting(EventCandidate::getDate)
                .containsExactly(
                        LocalDate.of(2026, 8, 5),
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 27));
        verify(client).newSession();
        verify(filter).method(Connection.Method.POST);
        verify(filter).data("competition", Integer.toString(competition));
        verify(filter).header("X-CSRF-TOKEN", "fresh-test-token");
        verify(filter).header("X-Requested-With", "XMLHttpRequest");
    }

    @Test
    void challengePageDoesNotTriggerBrowserOrCupPost() throws Exception {
        var client = spy(new EventSourceClient());
        var session = mock(Connection.class);
        var request = mock(Connection.class);
        var response = mock(Connection.Response.class);
        doReturn(session).when(client).newSession();
        when(session.newRequest(FakelEventParser.LIST_URL)).thenReturn(request);
        when(request.execute()).thenReturn(response);
        when(response.url()).thenReturn(URI.create(FakelEventParser.LIST_URL).toURL());
        when(response.body()).thenReturn("<html>Проверка браузера</html>");
        assertThatThrownBy(client::fakelCup)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("токен");
        verify(session, never()).newRequest(FakelEventParser.LIST_URL + "/list");
        assertThatThrownBy(() -> new FakelEventParser().parse(client.fakel(), LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("календарь");
    }

    @Test
    void malformedCupResponseIsNotTreatedAsEmptyCalendar() {
        assertThatThrownBy(() -> EventSourceClient.cupCalendar("{}"))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(
                        () ->
                                EventSourceClient.cupCalendar(
                                        "{\"grid\":\"\",\"select_seasons\":\"\"}"))
                .isInstanceOf(IOException.class);
    }

    @Test
    void validEmptyCupCalendarDoesNotProduceFalseErrorReport() throws Exception {
        String html =
                EventSourceClient.cupCalendar(
                        """
                        {"grid":"<div class='grid--matches'></div>",
                         "select_seasons":"<select name='season'><option>2026/2027</option></select>"}
                        """);
        assertThat(new FakelEventParser().parse(html, LocalDate.of(2026, 11, 1))).isEmpty();
    }

    private String cupFixture() throws Exception {
        try (var input = getClass().getResourceAsStream("/eventimport/fakel-cup.json")) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
