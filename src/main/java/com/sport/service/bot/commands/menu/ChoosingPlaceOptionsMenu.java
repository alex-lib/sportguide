package com.sport.service.bot.commands.menu;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;

public abstract class ChoosingPlaceOptionsMenu {

    public static InlineKeyboardMarkup createDistrictKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Железнодорожный", "ZHELEZNODOROZHNYY"),
                createButtonRow("Центральный", "CENTRALNYY"),
                createButtonRow("Коминтерновский", "KOMINTERNOVSKYY"),
                createButtonRow("Ленинский", "LENINSKYY"),
                createButtonRow("Советский", "SOVETSKYY"),
                createButtonRow("Левобережный", "LEVOBEREZNYY"),
                createButtonRow("За городом", "BEHIND_OF_CITY"),
                createButtonRow("Поиск по всем районам", "ALL_DISTRICTS")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createOutdoorKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Улица", "true"),
                createButtonRow("Помещение", "false")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static InlineKeyboardMarkup createTypeKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = List.of(
                createButtonRow("Уличная спортивная площадка", "SPORT_GROUND"),
                createButtonRow("Футбольное поле", "FOOTBALL_FIELD"),
                createButtonRow("Баскетбольное поле", "BASKETBALL_FIELD"),
                createButtonRow("Волейбольное поле", "VOLLEYBALL_FIELD"),
                createButtonRow("Теннисный корт", "TENNIS_COURT"),
                createButtonRow("Пинг-понг стол", "PINGPONG_TABLE"),
                createButtonRow("Падел корт", "PADEL_COURT"),
                createButtonRow("Ледовая арена", "ICE_RING"),
                createButtonRow("Бассейн", "SWIMMING_POOL"),
                createButtonRow("Беговая зона", "RUNNING_PLACE")
        );
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    private static List<InlineKeyboardButton> createButtonRow(String text, String callbackData) {
        return List.of(InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build());
    }
}