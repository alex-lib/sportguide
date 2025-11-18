package com.sport.service.bot.constants;

public final class MenuConstants {
    public static final String ADMIN_MENU = """
            \s
            Ты админ бота - можешь создавать/удалять спортивные места/события и пользоваться ботом как обычный юзер.
            
            Доступные команды:
            📍 Выбрать место - выбрать место
            📅 Ближайшие события - получить список грядущих событий
            🔔 Подписаться на уведомления - получать уведомления о спортивных событиях
            🚫 Отписаться от уведомлений - не получать уведомления о спортивных событиях
            ➕ Создать место - создать место
            ❌ Удалить место - удалить место
            ➕ Создать событие - создать событие
            ❌ Удалить событие - удалить событие
            🔎 Получить кол-во юзеров - кол-во юзеров стартанувших бот
            🔎 Получить кол-во подписок - кол-во подписок на получение событий
            ✉\uFE0F Отправить сообщение всем пользователям - ну ты понял :)
            \s""";

    public static final String SUBSCRIBER_MENU = """
            \s
            Доступные команды:
            📍 Выбрать место - выбрать место
            📅 Ближайшие события - получить список грядущих событий
            🔔 Подписаться на уведомления - получать уведомления о спортивных событиях
            🚫 Отписаться от уведомлений - не получать уведомления о спортивных событиях
            \uD83C\uDFCB\uFE0F\u200D♂\uFE0F Поддержать проект - поддержать проект
            \uD83E\uDE83 Связаться с админом - предложить сотрудничество, создать/скорректировать место или создать грядущее спортивное событие
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