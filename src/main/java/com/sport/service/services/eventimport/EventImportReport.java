package com.sport.service.services.eventimport;

import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class EventImportReport {
    private EventImportReport() {}

    public static String incomplete(EventCandidate event) {
        return render(event, false);
    }

    public static String existing(EventCandidate event) {
        return render(event, true);
    }

    private static String render(EventCandidate event, boolean existing) {
        StringBuilder text =
                new StringBuilder(
                        existing
                                ? "Не удалось проверить начало существующего события. Событие"
                                        + " сохранено без изменений.\n\n"
                                        + "Собранные данные:\n"
                                : "Не удалось автоматически создать событие.\n\n"
                                        + "Собранные данные:\n");
        field(text, "Источник", event.getSource());
        field(text, "Название", event.getName());
        field(text, "Описание", event.getDescription());
        field(text, "Дата", event.getDate());
        field(text, "Время", event.getTime());
        field(text, "Район", event.getDistrict());
        field(text, "Адрес", event.getAddress());
        field(text, "Место", event.getPlaceName());
        field(text, "Ссылка", event.getLink());
        field(text, "ID источника", event.getExternalId());
        text.append("\nНужно заполнить вручную: ")
                .append(
                        event.missingFields().isEmpty()
                                ? "все поля собраны; требуется проверка"
                                : String.join(", ", event.missingFields()));
        if (!event.getProblems().isEmpty())
            text.append("\nНужно проверить: ").append(String.join("; ", event.getProblems()));
        if (existing) {
            text.append(
                    "\n\nПроверьте источник и отредактируйте существующее событие в приложении.");
        } else
            text.append(
                    "\n\n"
                            + "Дополните данные и создайте событие вручную через /create_event."
                            + " Сохраните ссылку источника для сопоставления.");
        if (!event.getCollected().isEmpty()) {
            text.append("\n\nДополнительные данные источника:\n");
            event.getCollected().forEach((key, value) -> field(text, key, value));
        }
        return text.toString();
    }

    private static void field(StringBuilder text, String label, Object value) {
        text.append(label)
                .append(": ")
                .append(value == null || value.toString().isBlank() ? "не получено" : value)
                .append('\n');
    }

    public static String hash(String text) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<String> telegramParts(String text) {
        List<String> parts = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        text.codePoints()
                .forEach(
                        code -> {
                            String escaped =
                                    HtmlUtils.htmlEscape(new String(Character.toChars(code)));
                            if (chunk.length() + escaped.length() > 3500) {
                                parts.add(chunk.toString());
                                chunk.setLength(0);
                            }
                            chunk.append(escaped);
                        });
        if (!chunk.isEmpty()) parts.add(chunk.toString());
        return parts;
    }
}
