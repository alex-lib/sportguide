package com.sport.service.mappers.string;

import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.common.District;

public class DistrictStringMapper {

    public static String districtEnumToDistrictString(District district) {
        String districtString;
        switch (district) {
            case District.ZHELEZNODOROZHNYY -> districtString = KeyboardConstants.ZHELEZNODOROZHNYY;
            case District.KOMINTERNOVSKYY -> districtString = KeyboardConstants.KOMINTERNOVSKYY;
            case District.LEVOBEREZHNYY -> districtString = KeyboardConstants.LEVOBEREZHNYY;
            case District.CENTRALNYY -> districtString = KeyboardConstants.CENTRALNYY;
            case District.SOVETSKYY -> districtString = KeyboardConstants.SOVETSKYY;
            case District.LENINSKYY -> districtString = KeyboardConstants.LENINSKYY;
            case District.BEHIND_OF_CITY -> districtString = KeyboardConstants.BEHIND_OF_CITY;
            default -> districtString = KeyboardConstants.ALL_DISTRICTS;
        }
        return districtString;
    }

    public static District districtStringToDistrictEnum(String districtString) {
        District district;
        switch (districtString) {
            case KeyboardConstants.ZHELEZNODOROZHNYY -> district = District.ZHELEZNODOROZHNYY;
            case KeyboardConstants.KOMINTERNOVSKYY -> district = District.KOMINTERNOVSKYY;
            case KeyboardConstants.LEVOBEREZHNYY -> district = District.LEVOBEREZHNYY;
            case KeyboardConstants.CENTRALNYY -> district = District.CENTRALNYY;
            case KeyboardConstants.SOVETSKYY -> district = District.SOVETSKYY;
            case KeyboardConstants.LENINSKYY -> district = District.LENINSKYY;
            case KeyboardConstants.BEHIND_OF_CITY -> district = District.BEHIND_OF_CITY;
            default -> district = District.ALL_DISTRICTS;
        }
        return district;
    }

    public static District districtStringToDistrictEnumFromWeb(String districtString) {
        try {
            return District.valueOf(districtString);
        } catch (IllegalArgumentException e) {
            return District.ALL_DISTRICTS;
        }
    }
}