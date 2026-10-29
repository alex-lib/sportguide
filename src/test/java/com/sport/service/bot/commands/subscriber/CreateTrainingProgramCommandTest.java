package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.services.ai.TrainingGenerationService;
import com.sport.service.store.commands.CommandStateStore;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CreateTrainingProgramCommandTest {
    private final TelegramMessageSender sender = mock(TelegramMessageSender.class);
    private final CommandStateStore state = mock(CommandStateStore.class);
    private final TrainingGenerationService generation = mock(TrainingGenerationService.class);
    private final CreateTrainingProgramCommand command = new CreateTrainingProgramCommand(sender, state, generation);

    @Test
    void menuButtonStartsConversationAndChatPromptGeneratesTraining() {
        SendMessage subscriberMenu = new SendMessage();
        new SubscriberMenu(subscriberMenu).getSubscriberMenu();
        SendMessage adminMenu = new SendMessage();
        new AdminMenu(adminMenu).getAdminMenu();
        for (SendMessage menu : List.of(subscriberMenu, adminMenu)) {
            assertTrue(((ReplyKeyboardMarkup) menu.getReplyMarkup()).getKeyboard().stream()
                    .flatMap(List::stream)
                    .anyMatch(button -> MenuConstants.CREATE_TRAINING_PROGRAM.equals(button.getText())));
        }
        assertEquals("/" + command.getCommandIdentifier(),
                ButtonToCommandMapper.mapButtonToCommand(MenuConstants.CREATE_TRAINING_PROGRAM));

        Message message = message(MenuConstants.CREATE_TRAINING_PROGRAM);
        command.processMessage(null, message, new String[0]);
        verify(state).setCurrentCommand(10L, CommandsConstants.CREATE_TRAINING_PROGRAM);
        verify(sender).sendMessageWithoutPhoto(10L, CommandsConstants.CREATE_TRAINING_PROGRAM_START_TEXT);
        verifyNoInteractions(generation);

        when(state.getCurrentCommand(10L)).thenReturn(CommandsConstants.CREATE_TRAINING_PROGRAM);
        when(generation.generate(10L, "Бег")).thenReturn("Программа тренировки");
        message.setText("Бег");
        command.processTextInput(null, message);
        verify(generation).generate(10L, "Бег");
        verify(sender).sendMessageWithoutPhoto(10L, "Программа тренировки");
        verify(state).clearCurrentCommand(10L);
    }

    @Test
    void unrelatedChatMessagesDoNotGenerateTraining() {
        command.processTextInput(null, message("Бег"));
        verifyNoInteractions(generation, sender);
    }

    private Message message(String text) {
        User user = new User();
        user.setId(10L);
        Chat chat = new Chat();
        chat.setId(10L);
        Message message = new Message();
        message.setFrom(user);
        message.setChat(chat);
        message.setText(text);
        return message;
    }
}
