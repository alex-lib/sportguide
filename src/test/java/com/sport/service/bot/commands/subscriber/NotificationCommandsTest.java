package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.entities.Subscriber;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.repositories.SubscriberRepository;
import com.sport.service.services.NotificationPreferenceService;
import com.sport.service.store.notifications.Notification.NotificationType;
import com.sport.service.web.models.settings.NotificationPreferences;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationCommandsTest {
    @Test
    void botButtonsUpdateAllFiveSettingsAndKeepAdminBroadcastsEnabled() {
        SubscriberRepository repository = mock(SubscriberRepository.class);
        Subscriber subscriber = Subscriber.builder().id(10L).build();
        when(repository.findById(10L)).thenReturn(Optional.of(subscriber));
        NotificationPreferenceService preferences = new NotificationPreferenceService(repository);
        TelegramMessageSender sender = mock(TelegramMessageSender.class);
        Message message = new Message();
        User user = new User();
        user.setId(10L);
        message.setFrom(user);
        Chat chat = new Chat();
        chat.setId(10L);
        message.setChat(chat);

        new GetNotificationsCommand(preferences, sender).processMessage(null, message, new String[0]);
        assertEquals(new NotificationPreferences(true, true, true, true, true), preferences.get(10L));
        new StopNotificationsCommand(preferences, sender).processMessage(null, message, new String[0]);
        assertEquals(new NotificationPreferences(false, false, false, false, false), preferences.get(10L));
        assertTrue(preferences.isEnabled(10L, NotificationType.ADMIN_TO_SUBSCRIBER));
    }

    @Test
    void bothMenusHaveGlobalControlsAndMappingsUseMenuConstants() {
        SendMessage admin = new SendMessage();
        new AdminMenu(admin).getAdminMenu();
        SendMessage subscriber = new SendMessage();
        new SubscriberMenu(subscriber).getSubscriberMenu();
        for (SendMessage menu : List.of(admin, subscriber)) {
            var labels = ((ReplyKeyboardMarkup) menu.getReplyMarkup()).getKeyboard().stream()
                    .flatMap(List::stream).map(button -> button.getText()).toList();
            assertTrue(labels.contains(MenuConstants.SUBSCRIBE_TO_NOTIFICATIONS));
            assertTrue(labels.contains(MenuConstants.UNSUBSCRIBE_TO_NOTIFICATIONS));
        }
        assertEquals("/" + CommandsConstants.GET_NOTIFICATIONS,
                ButtonToCommandMapper.mapButtonToCommand(MenuConstants.SUBSCRIBE_TO_NOTIFICATIONS));
        assertEquals("/" + CommandsConstants.STOP_NOTIFICATIONS,
                ButtonToCommandMapper.mapButtonToCommand(MenuConstants.UNSUBSCRIBE_TO_NOTIFICATIONS));
        assertEquals("/" + CommandsConstants.DELETE_COACH,
                ButtonToCommandMapper.mapButtonToCommand(MenuConstants.DELETE_COACH));
        assertNull(ButtonToCommandMapper.mapButtonToCommand("/delete_coach"));
    }
}
