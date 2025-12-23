package com.sport.service.bot.constants;

public final class MenuConstants {
    public static final String ADMIN_MENU = """
            \s
            <u>Доступные команды:</u>
            📍 <b>Выбрать место</b>
            📅 <b>Ближайшие события</b>
            🔔 <b>Подписаться на уведомления</b>
            🚫 <b>Отписаться от уведомлений</b>
            ➕ <b>Создать место</b>
            ❌ <b>Удалить место</b>
            ➕ <b>Создать событие</b>
            ❌ <b>Удалить событие</b>
            🔎 <b>Получить кол-во юзеров</b>
            🔎 <b>Получить кол-во подписок</b>
            ✉\uFE0F Отправить сообщение всем пользователям</b>
            
            /menu - нажми, что бы начать :)
            \s""";

    public static final String SUBSCRIBER_MENU = """
            \s
            <u>Доступные команды:</u>
            📍 <b>Выбрать место</b>
            📅 <b>Ближайшие события</b> (получить список грядущих событий)
            🔔 <b>Подписаться на уведомления</b> (уведомления о спортивных событиях и погоде)
            🚫 <b>Отписаться от уведомлений</b>
            \uD83C\uDFCB\uFE0F\u200D♂\uFE0F <b>Поддержать проект</b>
            \uD83E\uDE83 <b>Связаться с админом</b> (сотрудничество, создать/скорректировать место/событие)
            
            /menu - нажми, что бы начать :)
            \s""";

    public static final String CHOOSE_PLACE = "📍 Выбрать место";
    public static final String UPCOMING_EVENTS = "📅 Ближайшие события";
    public static final String SUBSCRIBE_TO_NOTIFICATIONS = "🔔 Подписаться на уведомления";
    public static final String UNSUBSCRIBE_TO_NOTIFICATIONS = "🚫 Отписаться от уведомлений";
    public static final String SUPPORT_PROJECT = "\uD83C\uDFCB\uFE0F\u200D♂\uFE0F Поддержать проект";
    public static final String CONTACT_ADMIN = "\uD83E\uDE83 Связаться с админом";
    public static final String START = "/start";
    public static final String MENU = "/menu";

    public static final String CREATE_PLACE = "➕ Создать место";
    public static final String DELETE_PLACE = "❌ Удалить место";
    public static final String CREATE_EVENT = "➕ Создать событие";
    public static final String DELETE_EVENT = "❌ Удалить событие";
    public static final String SEND_MESSAGE_TO_ALL_USERS = "✉\uFE0F Отправить сообщение всем пользователям";
    public static final String GET_COUNT_OF_ALL_USERS = "🔎 Получить кол-во юзеров";
    public static final String GET_COUNT_OF_SUNSCRIPTIONS = "🔎 Получить кол-во подписавшихся юзеров";
}