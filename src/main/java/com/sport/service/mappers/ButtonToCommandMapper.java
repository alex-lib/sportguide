package com.sport.service.mappers;
import java.util.HashMap;
import java.util.Map;

public class ButtonToCommandMapper {
    private static final Map<String, String> BUTTON_COMMANDS = new HashMap<>();

    static {
        BUTTON_COMMANDS.put("/start", "/start");
        BUTTON_COMMANDS.put("/menu", "/menu");
        BUTTON_COMMANDS.put("📍 Выбрать место", "/get_place");
        BUTTON_COMMANDS.put("📅 Ближайшие события", "/get_upcoming_events");
        BUTTON_COMMANDS.put("🔔 Подписаться на уведомления", "/get_notifications");
        BUTTON_COMMANDS.put("🚫 Отписаться от уведомлений", "/stop_notifications");
        BUTTON_COMMANDS.put("➕ Создать место", "/create_place");
        BUTTON_COMMANDS.put("❌ Удалить место", "/delete_place");
        BUTTON_COMMANDS.put("➕ Создать событие", "/create_event");
        BUTTON_COMMANDS.put("❌ Удалить событие", "/delete_event");
        BUTTON_COMMANDS.put("\uD83C\uDFCB\uFE0F\u200D♂\uFE0F Поддержать проект", "/support_project");
        BUTTON_COMMANDS.put("\uD83E\uDE83 Связаться с админом", "/contact_admin");
        BUTTON_COMMANDS.put("🔎 Получить кол-во юзеров", "/get_users_count");
        BUTTON_COMMANDS.put("🔎 Получить кол-во подписавшихся юзеров", "/get_subscriptions_count");
        BUTTON_COMMANDS.put("✉\uFE0F Отправить сообщение всем пользователям", "/send_message_to_all_users");
    }

    public static String mapButtonToCommand(String buttonText) {
        return BUTTON_COMMANDS.get(buttonText);
    }
}