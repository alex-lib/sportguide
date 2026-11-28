package com.sport.service.mappers.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sport.service.entities.enums.common.SportType;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class SportTypeStringMapperTest {
    @Test
    void everySportHasADistinctLabelAndCanBeConvertedBack() {
        List<SportType> sports = Arrays.asList(SportType.values());
        List<String> labels = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(sports);
        assertEquals(sports.size(), labels.size());
        assertEquals(labels.size(), labels.stream().distinct().count());
        assertEquals(sports, SportTypeStringMapper.listSportTypeStringToListSportTypeEnum(labels));
    }

    @Test
    void poleDanceUsesTheSportLabel() {
        assertEquals(
                List.of("Спортивный пилон"),
                SportTypeStringMapper.listSportTypeEnumToListSportTypeString(
                        List.of(SportType.POLE_DANCE)));
    }
}
