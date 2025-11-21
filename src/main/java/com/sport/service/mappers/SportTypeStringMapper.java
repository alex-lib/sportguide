package com.sport.service.mappers;

import com.sport.service.entities.enums.common.SportType;

import java.util.ArrayList;
import java.util.List;

public class SportTypeStringMapper {

    public static List<String> listSportTypeEnumToListSportTypeString(List<SportType> sportTypes) {
        List<String> sportTypesStrings = new ArrayList<>();

        for (SportType sportType : sportTypes) {
            switch (sportType) {
                case SportType.FOOTBALL -> sportTypesStrings.add("Футбол");
                case SportType.VOLLEYBALL -> sportTypesStrings.add("Волейбол");
                case SportType.HOCKEY -> sportTypesStrings.add("Хоккей");
                case SportType.MMA -> sportTypesStrings.add("ММА");
                case SportType.BOXING -> sportTypesStrings.add("Бокс");
                case SportType.BASKETBALL -> sportTypesStrings.add("Баскетбол");
                case SportType.PADEL -> sportTypesStrings.add("Падел");
                case SportType.PING_PONG -> sportTypesStrings.add("Пинг-понг");
                case SportType.TENNIS -> sportTypesStrings.add("Теннис");
                case SportType.JIU_JITSU -> sportTypesStrings.add("Джиу-Джитсу");
                case SportType.WRESTLING -> sportTypesStrings.add("Борьба");
                case SportType.FITNESS -> sportTypesStrings.add("Фитнес");
                case SportType.SWIMMING -> sportTypesStrings.add("Плаванье");
                case SportType.NUTRITIONOLOGY -> sportTypesStrings.add("Нутрициология");
                case SportType.RUNNING -> sportTypesStrings.add("Бег");
                case SportType.WORKOUT -> sportTypesStrings.add("Воркаут");
                case SportType.SKIING -> sportTypesStrings.add("Лыжи");
                case SportType.SCATING -> sportTypesStrings.add("Фигурное катание");
                case SportType.GYMNASTICS -> sportTypesStrings.add("Гимнастика");
                case SportType.STRETCHING -> sportTypesStrings.add("Растяжка");
                case SportType.YOGA -> sportTypesStrings.add("Йога");
                case SportType.GROUP_TRAININGS -> sportTypesStrings.add("Групповые тренировки");
                case SportType.ATHLETICS -> sportTypesStrings.add("Легкая атлетика");
                case SportType.POWERLIFTING -> sportTypesStrings.add("Тяжелая атлетика");
                case SportType.RECOVERY -> sportTypesStrings.add("Восстановление");
                case SportType.DANCING -> sportTypesStrings.add("Танцы");
                case SportType.CYCLING -> sportTypesStrings.add("Велосипед");
            }
        }
        return sportTypesStrings;
    }

    public static List<SportType> listSportTypeStringToListSportTypeEnum(List<String> sportTypesStrings) {
        List<SportType> sportTypes = new ArrayList<>();

        for (String sportTypeString : sportTypesStrings) {
            switch (sportTypeString) {
                case "Футбол" -> sportTypes.add(SportType.FOOTBALL);
                case "Волейбол" -> sportTypes.add(SportType.VOLLEYBALL);
                case "Хоккей" -> sportTypes.add(SportType.HOCKEY);
                case "ММА" -> sportTypes.add(SportType.MMA);
                case "Бокс" -> sportTypes.add(SportType.BOXING);
                case "Баскетбол" -> sportTypes.add(SportType.BASKETBALL);
                case "Падел" -> sportTypes.add(SportType.PADEL);
                case "Пинг-понг" -> sportTypes.add(SportType.PING_PONG);
                case "Теннис" -> sportTypes.add(SportType.TENNIS);
                case "Джиу-Джитсу" -> sportTypes.add(SportType.JIU_JITSU);
                case "Борьба" -> sportTypes.add(SportType.WRESTLING);
                case "Фитнес" -> sportTypes.add(SportType.FITNESS);
                case "Плаванье" -> sportTypes.add(SportType.SWIMMING);
                case "Нутрициология" -> sportTypes.add(SportType.NUTRITIONOLOGY);
                case "Бег" -> sportTypes.add(SportType.RUNNING);
                case "Воркаут" -> sportTypes.add(SportType.WORKOUT);
                case "Лыжи" -> sportTypes.add(SportType.SKIING);
                case "Фигурное катание" -> sportTypes.add(SportType.SCATING);
                case "Гимнастика" -> sportTypes.add(SportType.GYMNASTICS);
                case "Растяжка" -> sportTypes.add(SportType.STRETCHING);
                case "Йога" -> sportTypes.add(SportType.YOGA);
                case "Групповые тренировки" -> sportTypes.add(SportType.GROUP_TRAININGS);
                case "Легкая атлетика" -> sportTypes.add(SportType.ATHLETICS);
                case "Тяжелая атлетика" -> sportTypes.add(SportType.POWERLIFTING);
                case "Восстановление" -> sportTypes.add(SportType.RECOVERY);
                case "Танцы" -> sportTypes.add(SportType.DANCING);
                case "Велосипед" -> sportTypes.add(SportType.CYCLING);
            }
        }
        return sportTypes;
    }
}