package com.sport.service.entities.enums.place;

import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.common.District;
import com.sport.service.services.PlaceService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@RequiredArgsConstructor
public enum CreatePlaceStep {
    DISTRICT {
        @Override
        public InlineKeyboardMarkup buildKeyboard(SendMessage answer, PlaceState state) {
            return ChoosingPlaceOptionsMenu.createDistrictKeyboard(answer);
        }

        @Override
        public CreatePlaceStep onCallback(String callbackData, PlaceState state, PlaceService placeService) {
            District district = District.valueOf(callbackData);
            state.setDistrict(district);
            if (!district.hasSubdistricts()) {
                state.setSubdistrict(null);
            }
            return NEXT;
        }
    },

    SUBDISTRICT {
        @Override
        public InlineKeyboardMarkup buildKeyboard(SendMessage answer, PlaceState state) {
            return state.getDistrict().buildSubdistrictsKeyboard(answer);
        }

        @Override
        public CreatePlaceStep onCallback(String callbackData, PlaceState state, PlaceService placeService) {
            state.setSubdistrict(SubDistrict.valueOf(callbackData));
            return NEXT;
        }
    },

    PLACE_TYPE {
        @Override
        public InlineKeyboardMarkup buildKeyboard(SendMessage answer, PlaceState state) {
            return ChoosingPlaceOptionsMenu.createPlaceTypeKeyboard(answer);
        }

        @Override
        public CreatePlaceStep onCallback(String callbackData, PlaceState state, PlaceService placeService) {
            state.setPlaceType(PlaceType.valueOf(callbackData));
            return NEXT;
        }
    },

    OUTDOOR {
        @Override
        public InlineKeyboardMarkup buildKeyboard(SendMessage answer, PlaceState state) {
            return ChoosingPlaceOptionsMenu.createOutdoorKeyboard(answer);
        }

        @Override
        public CreatePlaceStep onCallback(String callbackData, PlaceState state, PlaceService placeService) {
            if (!KeyboardConstants.NULL.equals(callbackData)) {
                state.setOutdoor(Boolean.parseBoolean(callbackData));
            } else {
                state.setOutdoor(null);
            }
            return NEXT;
        }
    },

    NAME(true) {
        @Override
        public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
            if (placeService.existsByName(text)) {
                answer.setText(CommandsConstants.PLACE_NAME_IS_EXISTED);
                return null; // stay on same step
            }
            state.setName(text);
            answer.setText(CommandsConstants.ENTER_PLACE_ADDRESS);
            return NEXT.name();
        }
    },

    ADDRESS(true) {
        @Override
        public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
            state.setAddress(text);
            answer.setText(CommandsConstants.ENTER_PLACE_DESCRIPTION);
            return NEXT.name();
        }
    },

    DESCRIPTION(true) {
        @Override
        public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
            state.setDescription(text);
            answer.setText(CommandsConstants.ENTER_PLACE_LINK);
            return NEXT.name();
        }
    },

    WEBSITE(true) {
        @Override
        public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
            state.setWebSite(text.equals("-") ? null : text);
            answer.setText(CommandsConstants.ENTER_PLACE_COORDINATES);
            return NEXT.name();
        }
    },

    COORDINATES(true) {
        @Override
        public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
            state.setCoordinates(text);
            answer.setText(CommandsConstants.SEND_PLACE_PHOTO);
            return NEXT.name();
        }
    },

    PHOTO(true);

    public final CreatePlaceStep NEXT;
    public final CreatePlaceStep PREV;

    private final boolean isTextInput;

    CreatePlaceStep() {
        this(false);
    }

    CreatePlaceStep(boolean isTextInput) {
        CreatePlaceStep[] all = values();
        int i = this.ordinal();
        this.NEXT = i < all.length - 1 ? all[i + 1] : null;
        this.PREV = i > 0 ? all[i - 1] : null;
        this.isTextInput = isTextInput;
    }

    public boolean isTextStep() {
        return isTextInput;
    }

    public boolean isCallbackStep() {
        return !isTextInput;
    }

    public boolean isFinished(PlaceState state) {
        return this == PHOTO;
    }

    public InlineKeyboardMarkup buildKeyboard(SendMessage answer, PlaceState state) {
        return null;
    }

    public CreatePlaceStep onCallback(String callbackData, PlaceState state, PlaceService placeService) {
        throw new UnsupportedOperationException();
    }

    public String handleText(SendMessage answer, String text, PlaceState state, PlaceService placeService) {
        throw new UnsupportedOperationException();
    }

    public boolean needsPhotoHandling() {
        return this == PHOTO;
    }
}
