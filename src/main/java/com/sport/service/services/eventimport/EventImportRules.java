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

    private static final Set<String> TITLE_ABBREVIATIONS =
            Set.of(
                    "ВХЛ", "КХЛ", "РПЛ", "ФНЛ", "МХЛ", "ФК", "ХК", "ЦСК", "ЦСКА", "ВВС",
                    "СКА", "ММА", "СССР", "РФ");
    private static final Pattern VORONEZH_PREFIX =
            Pattern.compile("(?iu)^\\s*(?:(?:г\\.?|город)\\s*)?воронеж(?=$|[\\s,;])\\s*[,;]?\\s*");
    private static final Pattern CYRILLIC_WORD = Pattern.compile("[А-ЯЁ]+");
    private static final Pattern QUOTED_TEAM = Pattern.compile("[«\"“]([^»\"”]+)[»\"”]");

    private EventImportRules() {}

    public static void apply(EventCandidate candidate) {
        if (candidate.getSource() == EventCandidate.Source.FAKEL) {
            candidate.setAddress("ул. Писателя Маршака, 1г");
            candidate.setDistrict(District.SOVETSKYY);
            return;
        }
        String hockeyTitle = buranMatchTitle(candidate.getName());
        candidate.setName(hockeyTitle == null ? readableTitle(candidate.getName()) : hockeyTitle);
        candidate.setDescription(
                hockeyTitle == null
                        ? firstSentence(candidate.getDescription())
                        : "Матч в рамках чемпионата ВХЛ.");
        candidate.setAddress(addressWithoutCity(candidate.getAddress()));
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

    static String addressWithoutCity(String address) {
        if (address == null) return null;
        return VORONEZH_PREFIX
                .matcher(address.replace('\u00a0', ' ').strip())
                .replaceFirst("")
                .strip();
    }

    static String readableTitle(String title) {
        if (EventCandidate.blank(title)) return title;
        String text = title.strip();
        if (!text.equals(text.toUpperCase(Locale.ROOT))) return text;
        text =
                CYRILLIC_WORD
                        .matcher(text)
                        .replaceAll(
                                match ->
                                        TITLE_ABBREVIATIONS.contains(match.group())
                                                ? match.group()
                                                : match.group().toLowerCase(Locale.ROOT));
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                return text.substring(0, i)
                        + Character.toUpperCase(text.charAt(i))
                        + text.substring(i + 1);
            }
        }
        return text;
    }

    static String buranMatchTitle(String title) {
        if (EventCandidate.blank(title)) return null;
        String[] teams = title.strip().split("\\s*[—–-]\\s*", 2);
        if (teams.length != 2
                || !normalize(teams[0]).matches("(?:хоккейный матч )?(?:хк )?буран(?: воронеж)?"))
            return null;
        if (teams[1].isBlank()) return null;
        String sourceTitle = title.strip().replaceFirst("(?iu)^хоккейный\\s+матч\\s+", "");
        return "Хоккейный матч " + sourceTitle;
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
        String hockeyTitle = buranMatchTitle(value);
        if (hockeyTitle != null) {
            // Keep matching legacy manually created short titles without changing the display name.
            String opponent = hockeyTitle.split("\\s*[—–-]\\s*", 2)[1];
            var quoted = QUOTED_TEAM.matcher(opponent);
            if (quoted.find()) opponent = quoted.group(1).strip();
            opponent =
                    opponent.replaceFirst(
                                    "(?iu)\\s*\\((?:Ижевск|Нефтекамск|Пермь|Самара|Воронеж)\\)\\s*$",
                                    "")
                            .replaceFirst("(?iu)^(Ижсталь) Ижевск$", "$1")
                            .replaceFirst("(?iu)^(Торос) Нефтекамск$", "$1")
                            .replaceFirst("(?iu)^(Молот) Пермь$", "$1")
                            .replaceFirst("(?iu)^(ЦСК ВВС) Самара$", "$1");
            value = "Буран " + opponent;
        }
        return normalize(value)
                .replaceFirst("^(футбольный|хоккейный) матч ", "")
                .replaceAll("\\bворонеж\\b", "")
                .replaceAll("\\s+", " ")
                .strip();
    }
}
