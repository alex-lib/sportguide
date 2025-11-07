package com.sport.service.bot.commands.menu;

import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.entities.place.District;
import com.sport.service.entities.place.PlaceType;
import com.sport.service.entities.place.Subdistrict;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public abstract class ChoosingPlaceOptionsMenu {

    public static InlineKeyboardMarkup createDistrictKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.ZHELEZNODOROZHNYY, District.ZHELEZNODOROZHNYY.name()),
                createButton(MenuConstants.KOMINTERNOVSKYY, District.KOMINTERNOVSKYY.name()),
                createButton(MenuConstants.LEVOBEREZHNYY, District.LEVOBEREZHNYY.name()),
                createButton(MenuConstants.CENTRALNYY, District.CENTRALNYY.name()),
                createButton(MenuConstants.LENINSKYY, District.LENINSKYY.name()),
                createButton(MenuConstants.SOVETSKYY, District.SOVETSKYY.name()),
                createButton(MenuConstants.BEHIND_OF_CITY, District.BEHIND_OF_CITY.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoDistrictKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoDistrictKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(MenuConstants.ALL_DISTRICTS, District.ALL_DISTRICTS.name()));
        }
        return keyboard;
    }

    public static InlineKeyboardMarkup createZheleznodorozhnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_SUBDISTRICT_OF_ZHELEZNODOROZHNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM, Subdistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM.name()),
                createButton(MenuConstants.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE, Subdistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE.name()),
                createButton(MenuConstants.ELECTRONIKA, Subdistrict.ELECTRONIKA.name()),
                createButton(MenuConstants.PROCESSOR, Subdistrict.PROCESSOR.name()),
                createButton(MenuConstants.OTROZHKA, Subdistrict.OTROZHKA.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createLevoberezhnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_SUBDISTRICT_OF_LEVOBEREZHNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.DIMITROVA_STREET, Subdistrict.DIMITROVA_STREET.name()),
                createButton(MenuConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE, Subdistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE.name()),
                createButton(MenuConstants.VAI_AND_QUARTER_KRASNYY_OKTYABR, Subdistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR.name()),
                createButton(MenuConstants.PESCHANKA_AND_OZERKI_AND_SHINNIK_1, Subdistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1.name()),
                createButton(MenuConstants.STARYY_MASHMET, Subdistrict.STARYY_MASHMET.name()),
                createButton(MenuConstants.BAM, Subdistrict.BAM.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createSovetskyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_SUBDISTRICT_OF_SOVETSKYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA, Subdistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA.name()),
                createButton(MenuConstants.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK, Subdistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK.name()),
                createButton(MenuConstants.PRIDONSKOYY_AND_PODKLETNOE, Subdistrict.PRIDONSKOYY_AND_PODKLETNOE.name()),
                createButton(MenuConstants.TENNISTYY_AND_ZAYYMISHE, Subdistrict.TENNISTYY_AND_ZAYYMISHE.name()),
                createButton(MenuConstants.SHILOVO, Subdistrict.SHILOVO.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createCentralnyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_SUBDISTRICT_OF_CENTRALNYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.REST_OF_CENTRALNYY_DISTRICT, Subdistrict.REST_OF_CENTRALNYY_DISTRICT.name()),
                createButton(MenuConstants.FROM_VGU_TO_SEVERNYY_BRIDGE, Subdistrict.FROM_VGU_TO_SEVERNYY_BRIDGE.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    public static InlineKeyboardMarkup createKominternovskyySubdistrictsKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_SUBDISTRICT_OF_KOMINTERNOVSKYY_DISTRICT);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP, Subdistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP.name()),
                createButton(MenuConstants.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET, Subdistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET.name()),
                createButton(MenuConstants.PODGORNOE, Subdistrict.PODGORNOE.name()),
                createButton(MenuConstants.SEVERNYY, Subdistrict.SEVERNYY.name())));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoSubdistrictsKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoSubdistrictsKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(MenuConstants.ALL_SUBDISTRICTS, Subdistrict.ALL_SUBDISTRICTS.name()));
            keyboard.add(createButton(MenuConstants.CHOOSE_DISTRICT_AGAIN, MenuConstants.BACK));
        }
        if (command.equals("creating")) {
            keyboard.add(createButton(MenuConstants.CHOOSE_DISTRICT_AGAIN, MenuConstants.BACK));
        }
        return keyboard;
    }

    public static InlineKeyboardMarkup createPlaceTypeKeyboard(SendMessage answer) {
        answer.setText(MenuConstants.CHOOSE_TYPE_OF_PLACE);
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButton(MenuConstants.SPORT_GROUND, PlaceType.SPORT_GROUND.name()),
                createButton(MenuConstants.FOOTBALL_FIELD, PlaceType.FOOTBALL_FIELD.name()),
                createButton(MenuConstants.BASKETBALL_FIELD, PlaceType.BASKETBALL_FIELD.name()),
                createButton(MenuConstants.VOLLEYBALL_FIELD, PlaceType.VOLLEYBALL_FIELD.name()),
                createButton(MenuConstants.TENNIS_COURT, PlaceType.TENNIS_COURT.name()),
                createButton(MenuConstants.PINGPONG_TABLE, PlaceType.PINGPONG_TABLE.name()),
                createButton(MenuConstants.PADEL_COURT, PlaceType.PADEL_COURT.name()),
                createButton(MenuConstants.ICE_RING, PlaceType.ICE_RING.name()),
                createButton(MenuConstants.SWIMMING_POOL, PlaceType.SWIMMING_POOL.name()),
                createButton(MenuConstants.RUNNING_PLACE, PlaceType.RUNNING_PLACE.name()),
                createButton(MenuConstants.MARTIAL_ARTS_HALL, PlaceType.MARTIAL_ARTS_HALL.name()),
                createButton(MenuConstants.GYM, PlaceType.GYM.name()),
                createButton(MenuConstants.CHOOSE_DISTRICT_OR_SUBDISTRICT_AGAIN, MenuConstants.BACK));
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createOutdoorKeyboard(SendMessage answer) {
        String command = answer.getText();
        answer.setText(MenuConstants.CHOOSE_OUTSIDE_OR_INSIDE);
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>(List.of(
                createButton(MenuConstants.OUTSIDE, MenuConstants.TRUE),
                createButton(MenuConstants.INSIDE, MenuConstants.FALSE)));
        return InlineKeyboardMarkup.builder().keyboard(addExtraButtonsIntoOutdoorKeyboard(keyboard, command)).build();
    }

    private static List<List<InlineKeyboardButton>> addExtraButtonsIntoOutdoorKeyboard(List<List<InlineKeyboardButton>> keyboard, String command) {
        if (command.equals("getting")) {
            keyboard.add(createButton(MenuConstants.OUTSIDE_AND_INSIDE, MenuConstants.NULL));
            keyboard.add(createButton(MenuConstants.CHOOSE_TYPE_OF_PLACE_AGAIN, MenuConstants.BACK));
        }
        if (command.equals("creating")) {
            keyboard.add(createButton(MenuConstants.CHOOSE_TYPE_OF_PLACE_AGAIN, MenuConstants.BACK));
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