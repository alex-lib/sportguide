package com.sport.service.bot.commands.admin;

import com.sport.service.aop.aspects.AdminOnlyAspect;
import com.sport.service.bot.SportGuideBot;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.services.CoachService;
import com.sport.service.services.JointTrainingService;
import com.sport.service.services.SubscriberService;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.JointTrainingRejectingSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;

class DeleteCoachCommandTest {
    private static final long USER_ID = 10L;
    private static final long CHAT_ID = 20L;
    private final CommandStateStore state = mock(CommandStateStore.class);
    private final TelegramMessageSender sender = mock(TelegramMessageSender.class);
    private final CoachService coaches = mock(CoachService.class);
    private final DeleteCoachCommand command = new DeleteCoachCommand(state, sender, coaches);

    @BeforeEach
    void setUp() {
        when(state.getCurrentCommand(USER_ID)).thenReturn(CommandsConstants.DELETE_COACH);
    }

    @Test
    void menuButtonStartsDeletion() {
        SendMessage menu = new SendMessage();
        new AdminMenu(menu).getAdminMenu();
        assertTrue(((ReplyKeyboardMarkup) menu.getReplyMarkup()).getKeyboard().stream()
                .flatMap(List::stream).anyMatch(button -> MenuConstants.DELETE_COACH.equals(button.getText())));
        SendMessage subscriberMenu = new SendMessage();
        new SubscriberMenu(subscriberMenu).getSubscriberMenu();
        assertFalse(((ReplyKeyboardMarkup) subscriberMenu.getReplyMarkup()).getKeyboard().stream()
                .flatMap(List::stream).anyMatch(button -> MenuConstants.DELETE_COACH.equals(button.getText())));
        SportGuideBot bot = new SportGuideBot("test", "test", List.of(command), state,
                mock(JointTrainingService.class), mock(JointTrainingRejectingSession.class), sender);
        ReflectionTestUtils.setField(bot, "secondAdminId", "10");
        Update update = new Update();
        update.setMessage(message(MenuConstants.DELETE_COACH));
        bot.onUpdateReceived(update);
        verify(state).setCurrentCommand(USER_ID, CommandsConstants.DELETE_COACH);
        verify(sender).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.DELETING_COACH_INSTRUCTION);
        verifyNoInteractions(coaches);
    }

    @Test
    void existingCoachIsDeletedAndSessionEnds() {
        when(coaches.deleteCoachByUsername("@coach_name")).thenReturn(true);
        command.processTextInput(null, message("  @coach_name  "));
        verify(coaches).deleteCoachByUsername("@coach_name");
        verify(state).clearCurrentCommand(USER_ID);
        verify(sender).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_DELETED);
    }

    @Test
    void unknownCoachKeepsSessionWithoutReportingSuccess() {
        command.processTextInput(null, message("@unknown_coach"));
        verify(coaches).deleteCoachByUsername("@unknown_coach");
        verify(state, never()).clearCurrentCommand(USER_ID);
        verify(sender).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_NOT_FOUND_FOR_DELETION);
        verify(sender, never()).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_DELETED);
    }

    @Test
    void cancellationDoesNotDeleteAnything() {
        command.processTextInput(null, message("/cancel"));
        verifyNoInteractions(coaches);
        verify(state).clearCurrentCommand(USER_ID);
        verify(sender).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.DELETE_COACH_CANCELLED);
    }

    @Test
    void inputOutsideDeletionSessionIsIgnored() {
        when(state.getCurrentCommand(USER_ID)).thenReturn(null);
        command.processTextInput(null, message("@coach_name"));
        verifyNoInteractions(coaches, sender);
    }

    @Test
    void failedDeletionDoesNotReportSuccess() {
        doThrow(new IllegalStateException("Deletion failed")).when(coaches).deleteCoachByUsername("@coach_name");
        command.processTextInput(null, message("@coach_name"));
        verify(state, never()).clearCurrentCommand(USER_ID);
        verify(sender, never()).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_DELETED);
    }

    @Test
    void invalidUsernameAllowsRetry() {
        when(coaches.deleteCoachByUsername("@@coach"))
                .thenThrow(new IllegalArgumentException("Введите Telegram-никнейм в формате @username."));
        command.processTextInput(null, message("@@coach"));
        verify(state).setCurrentCommand(USER_ID, CommandsConstants.DELETE_COACH);
        verify(state, never()).clearCurrentCommand(USER_ID);
        verify(sender, never()).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_DELETED);
    }

    @Test
    void adminRightsAreRequiredToStartAndCompleteDeletion() {
        SubscriberService subscribers = mock(SubscriberService.class);
        AspectJProxyFactory factory = new AspectJProxyFactory(command);
        factory.addAspect(new AdminOnlyAspect(subscribers));
        Object proxy = factory.getProxy();
        assertThrows(SecurityException.class, () -> ((IBotCommand) proxy)
                .processMessage(null, message(MenuConstants.DELETE_COACH), new String[0]));
        assertThrows(SecurityException.class, () -> ((TextProcessable) proxy)
                .processTextInput(null, message("@coach_name")));
        verifyNoInteractions(coaches);
    }

    private Message message(String text) {
        User user = new User();
        user.setId(USER_ID);
        user.setIsBot(false);
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        chat.setType("private");
        Message message = new Message();
        message.setFrom(user);
        message.setChat(chat);
        message.setText(text);
        if (text.startsWith("/")) {
            MessageEntity entity = new MessageEntity();
            entity.setType("bot_command");
            entity.setOffset(0);
            entity.setLength(text.length());
            message.setEntities(List.of(entity));
        }
        return message;
    }
}
