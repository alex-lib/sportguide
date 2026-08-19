package com.sport.service.mappers;

import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.MenuConstants;

import java.util.HashMap;
import java.util.Map;

public class ButtonToCommandMapper {
    private static final Map<String, String> BUTTON_COMMANDS = new HashMap<>();

    static {
        BUTTON_COMMANDS.put(MenuConstants.START, "/" + CommandsConstants.START);
        BUTTON_COMMANDS.put(MenuConstants.MENU, "/" + CommandsConstants.MENU);
        BUTTON_COMMANDS.put(MenuConstants.CHOOSE_PLACE, "/" + CommandsConstants.GET_PLACE);
        BUTTON_COMMANDS.put(MenuConstants.UPCOMING_EVENTS, "/" + CommandsConstants.GET_UPCOMING_EVENTS);
        BUTTON_COMMANDS.put(MenuConstants.SUBSCRIBE_TO_NOTIFICATIONS, "/" + CommandsConstants.GET_NOTIFICATIONS);
        BUTTON_COMMANDS.put(MenuConstants.UNSUBSCRIBE_TO_NOTIFICATIONS, "/" + CommandsConstants.STOP_NOTIFICATIONS);
        BUTTON_COMMANDS.put(MenuConstants.CREATE_PLACE, "/" + CommandsConstants.CREATE_PLACE);
        BUTTON_COMMANDS.put(MenuConstants.DELETE_PLACE, "/" + CommandsConstants.DELETE_PLACE);
        BUTTON_COMMANDS.put(MenuConstants.CREATE_EVENT, "/" + CommandsConstants.CREATE_EVENT);
        BUTTON_COMMANDS.put(MenuConstants.DELETE_EVENT, "/" + CommandsConstants.DELETE_EVENT);
        BUTTON_COMMANDS.put(MenuConstants.SUPPORT_PROJECT, "/" + CommandsConstants.SUPPORT_PROJECT);
        BUTTON_COMMANDS.put(MenuConstants.CONTACT_ADMIN, "/" + CommandsConstants.CONTACT_ADMIN);
        BUTTON_COMMANDS.put(MenuConstants.GET_COUNT_OF_ALL_USERS, "/" + CommandsConstants.GET_USERS_COUNT);
        BUTTON_COMMANDS.put(MenuConstants.GET_COUNT_OF_SUBSCRIPTIONS, "/" + CommandsConstants.GET_SUBSCRIPTIONS_COUNT);
        BUTTON_COMMANDS.put(MenuConstants.SEND_MESSAGE_TO_ALL_USERS, "/" + CommandsConstants.SEND_MESSAGE_TO_ALL_USERS);
        BUTTON_COMMANDS.put(MenuConstants.CREATE_TRAINING_PROGRAM, "/" + CommandsConstants.CREATE_TRAINING_PROGRAM);
        BUTTON_COMMANDS.put(MenuConstants.APP, "/" + CommandsConstants.APP);
    }

    public static String mapButtonToCommand(String buttonText) {
        return BUTTON_COMMANDS.get(buttonText);
    }
}