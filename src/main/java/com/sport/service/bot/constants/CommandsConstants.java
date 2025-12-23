package com.sport.service.bot.constants;

import java.util.regex.Pattern;

public final class CommandsConstants {
    public static final String CREATING_TYPE = "creating";
    public static final String GETTING_TYPE = "getting";

    public static final String START = "start";
    public static final String START_DESCRIPTION = "Launch bot and save user's data to database or find there uploaded data";

    public static final String MENU = "menu";
    public static final String MENU_DESCRIPTION = "Show user menu with buttons";
    public static final String MENU_MESSAGE = "Меню с кнопками представлены ниже ⬇\uFE0F";

    public static final String GET_PLACE = "get_place";
    public static final String GET_PLACE_DESCRIPTION = "Let user to get an appropriate sport places";
    public static final String NO_PLACES = "По выбранным параметрам места не найдены \uD83E\uDD37\u200D♂\uFE0F";

    public static final String GET_UPCOMING_EVENTS = "get_upcoming_events";
    public static final String GET_UPCOMING_EVENTS_DESCRIPTION = "Let subscribers get list of upcoming events";
    public static final String THERE_ARE_NO_EVENTS = "Ближайших событий нет \uD83E\uDD37\u200D♂\uFE0F";

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
    public static final String ENTER_TEXT = "📩 Напишите ваше предложение (ТОЛЬКО ТЕКСТ):";
    public static final String MESSAGE_SENT_TO_ADMIN = "Сообщение отправлено админу ✅";

    public static final String CREATE_PLACE = "create_place";
    public static final String CREATE_PLACE_DESCRIPTION = "Let admin create a new place";
    public static final String ENTER_PLACE_NAME = "\uD83D\uDD8A Введите название места:";
    public static final String ENTER_PLACE_ADDRESS = "\uD83D\uDD8A Введите адрес:";
    public static final String PLACE_NAME_IS_EXISTED = "Место с таким названием уже существует❗" + "\n" + "Попробуйте еще раз:";
    public static final String ENTER_PLACE_DESCRIPTION = "\uD83D\uDD8A Введите описание:";
    public static final String ENTER_PLACE_LINK = "\uD83D\uDD8A Введите сайт (или '-' если его нет):";
    public static final String ENTER_PLACE_COORDINATES = "\uD83D\uDCCD Введите координаты места, например их можно взять из Google maps (пример данных: 51.672628201614216, 39.261582161907924 или '-' если их нет):";
    public static final String SEND_PLACE_PHOTO = "\uD83D\uDDBC Отправьте фото:";
    public static final String SEND_PLACE_PHOTO_2 = "\uD83D\uDDBC Пожалуйста, отправьте фото:";
    public static final String PLACE_CREATED = "Место создано ✅";

    public static final String DELETE_PLACE = "delete_place";
    public static final String DELETE_PLACE_DESCRIPTION = "Let admin to delete a created place";
    public static final String DELETING_PLACE_INSTRUCTION = "Удалить место можно только по точному имени ранее сохраненного места. " +
            "Напишите название места, которое хотите удалить:";
    public static final String PLACE_DELETED = "Удаление места завершено ✅";

    public static final String CREATE_EVENT = "create_event";
    public static final String CREATE_EVENT_DESCRIPTION = "Let admin to create a new event";
    public static final Pattern DATE_PATTERN = Pattern.compile(
            "^([1-9]\\d{3})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$");
    public static final Pattern TIME_PATTERN = Pattern.compile(
            "^([01]\\d|2[0-3]):([0-5]\\d)$");
    public static final String ENTER_EVENT_NAME = "\uD83D\uDD8A Введите наименование события:";
    public static final String ENTER_EVENT_ADDRESS = "\uD83D\uDD8A Введите адрес:";
    public static final String EVENT_NAME_IS_EXISTED = "\uD83D\uDD8A Такое наименовение события уже существует.\nВведите другое наименование события:";
    public static final String ENTER_EVENT_DESCRIPTION = "\uD83D\uDD8A Введите описание:";
    public static final String ENTER_EVENT_LINK = "\uD83D\uDD8A Введите ссылку на событие (или '-' если нет):";
    public static final String ENTER_EVENT_PLACE = "\uD83D\uDD8A Введите имя места где будет организовано событие:";
    public static final String ENTER_EVENT_DATE = "\uD83D\uDD8A Введите дату в формате YYYY-MM-DD:";
    public static final String EVENT_DATE_IS_INVALID = "Неверный формат даты.\nВведите дату в формате YYYY-MM-DD:";
    public static final String ENTER_EVENT_TIME = "\uD83D\uDD8A Введите время в формате HH:mm:";
    public static final String EVENT_TIME_IS_INVALID = "Неверный формат времени.\nВведите время в формате HH:mm:";
    public static final String DATA_IS_RECEIVED = "Все данные получены.\nСоздаю событие ⏳";
    public static final String EVENT_CREATED = "Событие создано ✅";

    public static final String DELETE_EVENT = "delete_event";
    public static final String DELETE_EVENT_DESCRIPTION = "Let admin to delete a created event";
    public static final String DELETING_EVENT_INSTRUCTION = "Удалить событие можно только по точному имени ранее сохраненного события. " +
            "Напишите название события, которое хотите удалить:";
    public static final String EVENT_DELETED = "Удаление события завершено ✅";

    public static final String SEND_MESSAGE_TO_ALL_USERS = "send_message_to_all_users";
    public static final String SEND_MESSAGE_TO_ALL_USERS_DESCRIPTION = "Let admin send message to all users";
    public static final String ENTER_TEXT_TO_SEND_TO_ALL_USERS = "📩 Напишите, что вы хотите отправить подписчикам:";
    public static final String SEND_PHOTO_TO_SEND_TO_ALL_USERS = "\uD83D\uDDBC Отправьте фото или картинку (или - если картинки/фото нет):";
    public static final String SEND_PHOTO_TO_SEND_TO_ALL_USERS_2 = "\uD83D\uDDBC Пожалуйста, отправьте фото (или - если картинки/фото нет):";
    public static final String NO_PHOTO = "-";
    public static final String MESSAGE_SENT_TO_ALL_USERS = "Сообщение отправлено всем пользователям ✅";
    public static final String MESSAGE_SENT = "Сообщение отправлено всем пользователям ✅";

    public static final String GET_USERS_COUNT = "get_users_count";
    public static final String GET_USERS_COUNT_DESCRIPTION = "Let admin get count of users";
    public static final String GET_USERS_COUNT_TEXT = "\uD83E\uDDEE Количество юзеров: ";

    public static final String GET_SUBSCRIPTIONS_COUNT = "get_subscriptions_count";
    public static final String GET_SUBSCRIPTIONS_COUNT_DESCRIPTION = "Let admin get count of subscriptions to get notifications";
    public static final String GET_SUBSCRIPTIONS_COUNT_TEXT = "\uD83E\uDDEE Количество подписок на получение событий: ";

    public static final String GREETING_MESSAGE = """
            \s
            <i>Меня зовут</i> <b>SportGuide</b> - <i>спортивный бро bot :)</i>
            
            <i>Я постараюсь быть тебе полезным, что я могу сейчас:</i>
            
            <i>- уведомлять о грядущих спортивных событиях</i> \uD83C\uDFC6
            <i>- рассказать другим спортикам о твоем спортивном событии</i> \uD83D\uDCE8
            <i>- найти спортивные/активные места если тебе захочется найти другое место для тренировки/активити или узнать о местах, что бы начать заниматься спортом</i> ⛳\uFE0F
            <i>- высылать прогноз погоды на грядущий день, что бы ты с самого утра мог решить где будет тебе комфортнее заниматься - на улице или например в зале</i> \uD83C\uDF21
            
            <i>Я пока только изучаю город, поэтому если ты знаешь интересное спортивное место - будет здорово если ты сообщишь об этом моему напарнику /menu, заранее спасибо :)</i>
            \s""";
}