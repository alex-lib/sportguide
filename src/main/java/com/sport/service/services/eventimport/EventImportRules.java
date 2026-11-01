package com.sport.service.services.eventimport;

import com.sport.service.entities.enums.common.District;

import org.jsoup.Jsoup;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class EventImportRules {
    private static final Set<String> ABBREVIATIONS =
            Set.of("г", "ул", "д", "им", "пр", "т", "т.е", "т.д", "т.п");

    private EventImportRules() {}

    public static void apply(EventCandidate candidate) {
        if (candidate.getSource() == EventCandidate.Source.FAKEL) {
            candidate.setAddress("ул. Писателя Маршака, 1г");
            candidate.setDistrict(District.SOVETSKYY);
            return;
        }
        candidate.setDescription(firstSentence(candidate.getDescription()));
        String place = normalize(candidate.getPlaceName());
        String address = normalize(candidate.getAddress());
        if (place.contains("aura")
                || place.contains("аура")
                || (address.contains("революции")
                        && (address.contains("проспект")
                                || address.contains("пр кт")
                                || address.contains("пр т")))) {
            candidate.setDistrict(District.CENTRALNYY);
        } else if (place.contains("юбилейный")) {
            candidate.setPlaceName("СК Юбилейный");
            candidate.setDistrict(District.CENTRALNYY);
            if (EventCandidate.blank(candidate.getAddress())) {
                candidate.setAddress("ул. Карла Маркса, 116");
            }
        }
    }

    public static String firstSentence(String html) {
        if (EventCandidate.blank(html)) return null;
        String text = Jsoup.parse(html).text().replace('\u00a0', ' ').strip();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch != '.' && ch != '!' && ch != '?') continue;
            if (ch == '.' && i + 1 < text.length() && Character.isDigit(text.charAt(i + 1)))
                continue;
            String prefix = text.substring(0, i);
            String token = prefix.substring(prefix.lastIndexOf(' ') + 1);
            if (ch == '.'
                    && (ABBREVIATIONS.contains(token.toLowerCase(Locale.ROOT))
                            || token.matches("[А-ЯA-Z]"))) continue;
            int end = i + 1;
            while (end < text.length() && ".!?»\"”.".indexOf(text.charAt(end)) >= 0) end++;
            if (end == text.length() || Character.isWhitespace(text.charAt(end)))
                return text.substring(0, end);
        }
        return text;
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .strip();
    }

    public static String matchName(String value) {
        if (value != null && value.contains("Буран")) {
            var quoted = Pattern.compile("«([^»]+)»").matcher(value);
            java.util.List<String> teams = new java.util.ArrayList<>();
            while (quoted.find()) teams.add(quoted.group(1));
            if (teams.size() == 2) return normalize(String.join(" ", teams));
        }
        return normalize(value)
                .replaceFirst("^(футбольный|хоккейный) матч ", "")
                .replaceAll("\\bворонеж\\b", "")
                .replaceAll("\\s+", " ")
                .strip();
    }
}
