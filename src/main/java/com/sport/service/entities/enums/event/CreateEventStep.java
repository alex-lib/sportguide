package com.sport.service.entities.enums.event;

import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.entities.enums.common.District;
import com.sport.service.services.EventService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public enum CreateEventStep {
    DISTRICT(true) {
        @Override
        public InlineKeyboardMarkup buildKeyboard(SendMessage answer, EventState state) {
            return ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer);
        }

        @Override
        public CreateEventStep onCallback(String callbackData, EventState state, EventService eventService) {
            District district = District.valueOf(callbackData);
            state.setDistrict(district);
            answer.setText(CommandsConstants.ENTER_EVENT_NAME);
            return NEXT;
        }
    },

    NAME(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            if (eventService.existsEventByName(text)) {
                answer.setText(CommandsConstants.EVENT_NAME_IS_EXISTED);
                return null;
            }
            state.setName(text);
            answer.setText(CommandsConstants.ENTER_EVENT_ADDRESS);
            return NEXT.name();
        }
    },

    ADDRESS(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            state.setAddress(text);
            answer.setText(CommandsConstants.ENTER_EVENT_DESCRIPTION);
            return NEXT.name();
        }
    },

    DESCRIPTION(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            state.setDescription(text);
            answer.setText(CommandsConstants.ENTER_EVENT_LINK);
            return NEXT.name();
        }
    },

    LINK(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            state.setLink(text);
            answer.setText(CommandsConstants.ENTER_EVENT_PLACE);
            return NEXT.name();
        }
    },

    PLACE(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            state.setPlaceName(text);
            answer.setText(CommandsConstants.ENTER_EVENT_DATE);
            return NEXT.name();
        }
    },

    DATE(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            if (!CommandsConstants.DATE_PATTERN.matcher(text).matches()) {
                answer.setText(CommandsConstants.EVENT_DATE_IS_INVALID);
                return null;
            }
            state.setDate(text);
            answer.setText(CommandsConstants.ENTER_EVENT_TIME);
            return NEXT.name();
        }
    },

    TIME(false) {
        @Override
        public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
            if (!CommandsConstants.TIME_PATTERN.matcher(text).matches()) {
                answer.setText(CommandsConstants.EVENT_TIME_IS_INVALID);
                return null;
            }
            state.setTime(text);
            answer.setText(CommandsConstants.DATA_IS_RECEIVED);
            return NEXT.name();
        }
    },

    CREATED(false);

    public final CreateEventStep NEXT;
    public final CreateEventStep PREV;

    private final boolean isTextStep;
    private final boolean isCallbackStep;

    CreateEventStep(boolean isTextStep) {
        this(isTextStep, false);
    }

    CreateEventStep(boolean isTextStep, boolean isCallbackStep) {
        CreateEventStep[] all = values();
        int i = this.ordinal();
        this.NEXT = i < all.length - 1 ? all[i + 1] : null;
        this.PREV = i > 0 ? all[i - 1] : null;
        this.isTextStep = isTextStep;
        this.isCallbackStep = isCallbackStep;
    }

    public boolean isTextStep() {
        return isTextStep;
    }

    public boolean isCallbackStep() {
        return isCallbackStep;
    }

    public boolean isFinished() {
        return this == CREATED;
    }

    public InlineKeyboardMarkup buildKeyboard(SendMessage answer, EventState state) {
        return null;
    }

    public CreateEventStep onCallback(String callbackData, EventState state, EventService eventService) {
        throw new UnsupportedOperationException();
    }

    public String handleText(SendMessage answer, String text, EventState state, EventService eventService) {
        throw new UnsupportedOperationException();
    }
}
