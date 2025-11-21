package com.sport.service.mappers;

public class OutdoorStringMapper {

    public static String outdoorEnumToOutdoorString(Boolean outdoor) {
        return outdoor ? "Улица" : "Помещение";
    }

    public static Boolean outdoorStringToOutdoorEnum(String outdoorString) {
        Boolean outdoor;
        switch (outdoorString) {
            case "Улица" -> outdoor = Boolean.TRUE;
            case "Помещение" -> outdoor = Boolean.FALSE;
            default -> outdoor = null;
        }
        return outdoor;
    }
}