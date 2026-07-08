package com.sport.service.mappers.string;

import com.sport.service.entities.enums.coach.Sex;

import static com.sport.service.entities.enums.coach.Sex.FEMALE;
import static com.sport.service.entities.enums.coach.Sex.MALE;

public class SexStringMapper {
    public static String sexEnumToSexString(Sex sex) {
        if (sex == null) {
            return null;
        }
        return switch (sex) {
            case MALE -> "Мужчина";
            case FEMALE -> "Женщина";
        };
    }

    public static Sex sexStringToSexEnum(String sexString) {
        Sex sex;
        switch (sexString) {
            case "Мужчина" -> sex = MALE;
            case "Женщина" -> sex = FEMALE;
            default -> sex = null;
        }
        return sex;
    }
}
