package com.sport.service.mappers.string;

public class OutdoorStringMapper {

    public static String outdoorEnumToOutdoorString(Boolean outdoor) {
        return outdoor ? "Улица" : "Помещение";
    }

    public static Boolean outdoorStringToOutdoorEnum(String outdoorString) {
        Boolean outdoor;
        if (outdoorString == null || outdoorString.isEmpty()) {
            outdoor = null;
        } else if (outdoorString.startsWith("Улица")) {
            outdoor = Boolean.TRUE;
        } else if (outdoorString.startsWith("Помещение")) {
            outdoor = Boolean.FALSE;
        } else {
            outdoor = null;
        }
        return outdoor;
    }
}