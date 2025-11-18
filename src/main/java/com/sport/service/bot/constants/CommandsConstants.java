package com.sport.service.bot.constants;

public final class CommandsConstants {
    public static final String START = "start";
    public static final String START_DESCRIPTION = "Launch bot and save user's data to database or find there uploaded data";

    public static final String MENU = "menu";
    public static final String MENU_DESCRIPTION = "Show user menu with buttons";

    public static final String GET_PLACE = "get_place";
    public static final String GET_PLACE_DESCRIPTION = "Let user to get an appropriate sport places";

    public static final String GET_UPCOMING_EVENTS = "get_upcoming_events";
    public static final String GET_UPCOMING_EVENTS_DESCRIPTION = "Let subscribers get list of upcoming events";

    public static final String GET_NOTIFICATIONS = "get_notifications";
    public static final String GET_NOTIFICATIONS_DESCRIPTION = "Change variable getEvents to true";
    public static final String GET_NOTIFICATIONS_ALREADY_GET_TEXT = "Вы уже подписаны на получение уведомлений \uD83D\uDC4C";
    public static final String GET_NOTIFICATIONS_START_TEXT = "Теперь вы будете получать уведомление при создании события ✅";

    public static final String STOP_NOTIFICATIONS = "stop_notifications";
    public static final String STOP_NOTIFICATIONS_DESCRIPTION = "Change variable getEvents to false";
    public static final String STOP_NOTIFICATIONS_ALREADY_STOP_TEXT = "Вы не подписаны на получение уведомлений \uD83D\uDC4C";
    public static final String STOP_NOTIFICATIONS_STOP_TEXT = "Теперь вы не будете получать уведомление при создании события ✅";

    public static final String SUPPORT_PROJECT = "support_project";
    public static final String SUPPORT_PROJECT_DESCRIPTION = "Give to subscriber info to support project";
    public static final String SUPPORT_PROJECT_TEXT = """
            \s
            USDT
            address: 0x53a43924e55251d4a73023a4ee0e7188ffc978fa
            network: Arbitrum One
            THANK YOU🙏
            \s""";

    public static final String CONTACT_ADMIN = "contact_admin";
    public static final String CONTACT_ADMIN_DESCRIPTION = "Let subscriber to contact admin";

    public static final String CREATE_PLACE = "create_place";
    public static final String CREATE_PLACE_DESCRIPTION = "Let admin create a new place";

    public static final String DELETE_PLACE = "delete_place";
    public static final String DELETE_PLACE_DESCRIPTION = "Let admin to delete a created place";

    public static final String CREATE_EVENT = "create_event";
    public static final String CREATE_EVENT_DESCRIPTION = "Let admin to create a new event";

    public static final String DELETE_EVENT = "delete_event";
    public static final String DELETE_EVENT_DESCRIPTION = "Let admin to delete a created event";

    public static final String SEND_MESSAGE_TO_ALL_USERS = "send_message_to_all_users";
    public static final String SEND_MESSAGE_TO_ALL_USERS_DESCRIPTION = "Let admin send message to all users";

    public static final String GET_USERS_COUNT = "get_users_count";
    public static final String GET_USERS_COUNT_DESCRIPTION = "Let admin get count of users";
    public static final String GET_USERS_COUNT_TEXT = "\uD83E\uDDEE Количество юзеров: ";

    public static final String GET_SUBSCRIPTIONS_COUNT = "get_subscriptions_count";
    public static final String GET_SUBSCRIPTIONS_COUNT_DESCRIPTION = "Let admin get count of subscriptions to get notifications";
    public static final String GET_SUBSCRIPTIONS_COUNT_TEXT = "\uD83E\uDDEE Количество подписок на получение событий: ";
}
