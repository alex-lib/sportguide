package com.sport.service.mappers.string;

import com.sport.service.entities.enums.coach.Sex;

import static com.sport.service.entities.enums.coach.Sex.FEMALE;
import static com.sport.service.entities.enums.coach.Sex.MALE;

public class SexStringMapper {
    public static String sexEnumToSexString(Sex sex) {
        return null;
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
