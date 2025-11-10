package com.sport.service.entities.place;

import com.sport.service.bot.commands.menu.ChoosingPlaceOptionsMenu;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.function.Function;

public enum District {
    ZHELEZNODOROZHNYY(ChoosingPlaceOptionsMenu::createZheleznodorozhnyySubdistrictsKeyboard),
    KOMINTERNOVSKYY(ChoosingPlaceOptionsMenu::createKominternovskyySubdistrictsKeyboard),
    LEVOBEREZHNYY(ChoosingPlaceOptionsMenu::createLevoberezhnyySubdistrictsKeyboard),
    CENTRALNYY(ChoosingPlaceOptionsMenu::createCentralnyySubdistrictsKeyboard),
    SOVETSKYY(ChoosingPlaceOptionsMenu::createSovetskyySubdistrictsKeyboard),
    LENINSKYY(null),
    BEHIND_OF_CITY(null),
    ALL_DISTRICTS(null);

    private final Function<SendMessage, InlineKeyboardMarkup> subdistrictKeyboardBuilder;

    District(Function<SendMessage, InlineKeyboardMarkup> subdistrictKeyboardBuilder) {
        this.subdistrictKeyboardBuilder = subdistrictKeyboardBuilder;
    }

    public boolean hasSubdistricts() {
        return subdistrictKeyboardBuilder != null;
    }

    public InlineKeyboardMarkup buildSubdistrictsKeyboard(SendMessage answer) {
        return subdistrictKeyboardBuilder.apply(answer);
    }
}