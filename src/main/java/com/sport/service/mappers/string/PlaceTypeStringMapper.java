package com.sport.service.mappers.string;

import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.place.PlaceType;

public class PlaceTypeStringMapper {

    public static String placeTypeEnumToPlaceTypeString(PlaceType placeType) {
        String placeTypeString;
        switch (placeType) {
            case PlaceType.SPORT_GROUND -> placeTypeString = KeyboardConstants.SPORT_GROUND;
            case PlaceType.FOOTBALL_FIELD -> placeTypeString = KeyboardConstants.FOOTBALL_FIELD;
            case PlaceType.BASKETBALL_FIELD -> placeTypeString = KeyboardConstants.BASKETBALL_FIELD;
            case PlaceType.VOLLEYBALL_FIELD -> placeTypeString = KeyboardConstants.VOLLEYBALL_FIELD;
            case PlaceType.TENNIS_COURT -> placeTypeString = KeyboardConstants.TENNIS_COURT;
            case PlaceType.PINGPONG_TABLE -> placeTypeString = KeyboardConstants.PINGPONG_TABLE;
            case PlaceType.PADEL_COURT -> placeTypeString = KeyboardConstants.PADEL_COURT;
            case PlaceType.ICE_RING -> placeTypeString = KeyboardConstants.ICE_RING;
            case PlaceType.SWIMMING_POOL -> placeTypeString = KeyboardConstants.SWIMMING_POOL;
            case PlaceType.RUNNING_PLACE -> placeTypeString = KeyboardConstants.RUNNING_PLACE;
            case PlaceType.MARTIAL_ARTS_HALL -> placeTypeString = KeyboardConstants.MARTIAL_ARTS_HALL;
            case PlaceType.GYM -> placeTypeString = KeyboardConstants.GYM;
            default -> placeTypeString = null;
        }
        return placeTypeString;
    }

    public static PlaceType placeTypeStringToPlaceTypeEnum(String placeTypeString) {
        PlaceType placeType;
        switch (placeTypeString) {
            case KeyboardConstants.SPORT_GROUND -> placeType = PlaceType.SPORT_GROUND;
            case KeyboardConstants.FOOTBALL_FIELD -> placeType = PlaceType.FOOTBALL_FIELD;
            case KeyboardConstants.BASKETBALL_FIELD -> placeType = PlaceType.BASKETBALL_FIELD;
            case KeyboardConstants.VOLLEYBALL_FIELD -> placeType = PlaceType.VOLLEYBALL_FIELD;
            case KeyboardConstants.TENNIS_COURT -> placeType = PlaceType.TENNIS_COURT;
            case KeyboardConstants.PINGPONG_TABLE -> placeType = PlaceType.PINGPONG_TABLE;
            case KeyboardConstants.PADEL_COURT -> placeType = PlaceType.PADEL_COURT;
            case KeyboardConstants.ICE_RING -> placeType = PlaceType.ICE_RING;
            case KeyboardConstants.SWIMMING_POOL -> placeType = PlaceType.SWIMMING_POOL;
            case KeyboardConstants.RUNNING_PLACE -> placeType = PlaceType.RUNNING_PLACE;
            case KeyboardConstants.MARTIAL_ARTS_HALL -> placeType = PlaceType.MARTIAL_ARTS_HALL;
            case KeyboardConstants.GYM -> placeType = PlaceType.GYM;
            default -> placeType = null;
        }
        return placeType;
    }
}