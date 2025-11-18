package com.sport.service.bot.commands.menu;

import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.place.PlaceType;
import com.sport.service.entities.enums.place.Subdistrict;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoosingPlaceOptionsMenu {

    public static InlineKeyboardMarkup createDistrictKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.ZHELEZNODOROZHNYY, District.ZHELEZNODOROZHNYY.name()),
                createButton(KeyboardConstants.KOMINTERNOVSKYY, District.KOMINTERNOVSKYY.name()),
                createButton(KeyboardConstants.LEVOBEREZHNYY, District.LEVOBEREZHNYY.name()),
                createButton(KeyboardConstants.CENTRALNYY, District.CENTRALNYY.name()),
                createButton(KeyboardConstants.LENINSKYY, District.LENINSKYY.name()),
                createButton(KeyboardConstants.SOVETSKYY, District.SOVETSKYY.name()),
                createButton(KeyboardConstants.BEHIND_OF_CITY, District.BEHIND_OF_CITY.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoDistrictKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoDistrictKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(KeyboardConstants.ALL_DISTRICTS, District.ALL_DISTRICTS.name()));
        }
        return keyboard;
    }

    public static InlineKeyboardMarkup createZheleznodorozhnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_SUBDISTRICT_OF_ZHELEZNODOROZHNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM, Subdistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM.name()),
                createButton(KeyboardConstants.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE, Subdistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE.name()),
                createButton(KeyboardConstants.ELECTRONIKA, Subdistrict.ELECTRONIKA.name()),
                createButton(KeyboardConstants.PROCESSOR, Subdistrict.PROCESSOR.name()),
                createButton(KeyboardConstants.OTROZHKA, Subdistrict.OTROZHKA.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createLevoberezhnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_SUBDISTRICT_OF_LEVOBEREZHNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.DIMITROVA_STREET, Subdistrict.DIMITROVA_STREET.name()),
                createButton(KeyboardConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE, Subdistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE.name()),
                createButton(KeyboardConstants.VAI_AND_QUARTER_KRASNYY_OKTYABR, Subdistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR.name()),
                createButton(KeyboardConstants.PESCHANKA_AND_OZERKI_AND_SHINNIK_1, Subdistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1.name()),
                createButton(KeyboardConstants.STARYY_MASHMET, Subdistrict.STARYY_MASHMET.name()),
                createButton(KeyboardConstants.BAM, Subdistrict.BAM.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createSovetskyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_SUBDISTRICT_OF_SOVETSKYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA, Subdistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA.name()),
                createButton(KeyboardConstants.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK, Subdistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK.name()),
                createButton(KeyboardConstants.PRIDONSKOYY_AND_PODKLETNOE, Subdistrict.PRIDONSKOYY_AND_PODKLETNOE.name()),
                createButton(KeyboardConstants.TENNISTYY_AND_ZAYYMISHE, Subdistrict.TENNISTYY_AND_ZAYYMISHE.name()),
                createButton(KeyboardConstants.SHILOVO, Subdistrict.SHILOVO.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createCentralnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_SUBDISTRICT_OF_CENTRALNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.REST_OF_CENTRALNYY_DISTRICT, Subdistrict.REST_OF_CENTRALNYY_DISTRICT.name()),
                createButton(KeyboardConstants.FROM_VGU_TO_SEVERNYY_BRIDGE, Subdistrict.FROM_VGU_TO_SEVERNYY_BRIDGE.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createKominternovskyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_SUBDISTRICT_OF_KOMINTERNOVSKYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP, Subdistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP.name()),
                createButton(KeyboardConstants.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET, Subdistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET.name()),
                createButton(KeyboardConstants.PODGORNOE, Subdistrict.PODGORNOE.name()),
                createButton(KeyboardConstants.SEVERNYY, Subdistrict.SEVERNYY.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoSubdistrictsKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(KeyboardConstants.ALL_SUBDISTRICTS, Subdistrict.ALL_SUBDISTRICTS.name()));
            keyboard.add(createButton(KeyboardConstants.CHOOSE_DISTRICT_AGAIN, KeyboardConstants.BACK));
        }
        if (command.equals("creating")) {
            keyboard.add(createButton(KeyboardConstants.CHOOSE_DISTRICT_AGAIN, KeyboardConstants.BACK));
        }
        return keyboard;
    }

    public static InlineKeyboardMarkup createPlaceTypeKeyboard(SendMessage answer) {
        answer.setText(KeyboardConstants.CHOOSE_TYPE_OF_PLACE);
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButton(KeyboardConstants.SPORT_GROUND, PlaceType.SPORT_GROUND.name()),
                createButton(KeyboardConstants.FOOTBALL_FIELD, PlaceType.FOOTBALL_FIELD.name()),
                createButton(KeyboardConstants.BASKETBALL_FIELD, PlaceType.BASKETBALL_FIELD.name()),
                createButton(KeyboardConstants.VOLLEYBALL_FIELD, PlaceType.VOLLEYBALL_FIELD.name()),
                createButton(KeyboardConstants.TENNIS_COURT, PlaceType.TENNIS_COURT.name()),
                createButton(KeyboardConstants.PINGPONG_TABLE, PlaceType.PINGPONG_TABLE.name()),
                createButton(KeyboardConstants.PADEL_COURT, PlaceType.PADEL_COURT.name()),
                createButton(KeyboardConstants.ICE_RING, PlaceType.ICE_RING.name()),
                createButton(KeyboardConstants.SWIMMING_POOL, PlaceType.SWIMMING_POOL.name()),
                createButton(KeyboardConstants.RUNNING_PLACE, PlaceType.RUNNING_PLACE.name()),
                createButton(KeyboardConstants.MARTIAL_ARTS_HALL, PlaceType.MARTIAL_ARTS_HALL.name()),
                createButton(KeyboardConstants.GYM, PlaceType.GYM.name()),
                createButton(KeyboardConstants.CHOOSE_DISTRICT_OR_SUBDISTRICT_AGAIN, KeyboardConstants.BACK));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createOutdoorKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(KeyboardConstants.CHOOSE_OUTSIDE_OR_INSIDE);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(KeyboardConstants.OUTSIDE, KeyboardConstants.TRUE),
                createButton(KeyboardConstants.INSIDE, KeyboardConstants.FALSE)));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoOutdoorKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoOutdoorKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(KeyboardConstants.OUTSIDE_AND_INSIDE, KeyboardConstants.NULL));
            keyboard.add(createButton(KeyboardConstants.CHOOSE_TYPE_OF_PLACE_AGAIN, KeyboardConstants.BACK));
        }
        if (command.equals("creating")) {
            keyboard.add(createButton(KeyboardConstants.CHOOSE_TYPE_OF_PLACE_AGAIN, KeyboardConstants.BACK));
        }
        return keyboard;
    }

    private static List<InlineKeyboardButton> createButton(String text, String callbackData) {
        return new ArrayList<>(List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build()));
    }
}