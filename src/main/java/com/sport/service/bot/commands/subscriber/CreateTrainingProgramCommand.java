package com.sport.service.bot.commands.subscriber;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.redis_store.commands_store.CommandStateStore;
import com.sport.service.services.ai.AiAssistantService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

// ... existing code ...

@Component
public class CreateTrainingProgramCommand implements IBotCommand {

    // Имя состояния для CommandStateStore
    private static final String STATE_WAITING_INPUT = "CREATE_TRAINING_PROGRAM_WAIT_INPUT";
    // Текст кнопки (должен совпадать с текстом в меню)
    private static final String BUTTON_TEXT = "\uD83E\uDD16 Создать тренировку";

    private final TelegramMessageSender sender;
    private final CommandStateStore commandStateStore;
    private final AiAssistantService aiAssistantService;

    public CreateTrainingProgramCommand(TelegramMessageSender sender,
                                        CommandStateStore commandStateStore,
                                        AiAssistantService aiAssistantService) {
        this.sender = sender;
        this.commandStateStore = commandStateStore;
        this.aiAssistantService = aiAssistantService;
    }


    public boolean supports(Message message) {
        if (message == null || message.getText() == null) return false;

        String text = message.getText().trim();
        Long chatId = message.getChatId();

        boolean buttonPressed = BUTTON_TEXT.equalsIgnoreCase(text);
        boolean waitingInput = STATE_WAITING_INPUT.equals(commandStateStore.getCurrentCommand(chatId));

        return buttonPressed || waitingInput;
    }

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.CREATE_TRAINING_PROGRAM;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] strings) {
        Long chatId = message.getChatId();
        String text = message.getText() == null ? "" : message.getText().trim();

        // Первый шаг: пользователь нажал кнопку
        if (BUTTON_TEXT.equalsIgnoreCase(text)) {
            String prompt = "Введите вид спорта или группу/группы мышц, для которой хотите получить тренировку. " +
                    "Также вы можете указать дополнительные пожелания или сообщить о наличии травм.";
            sender.sendMessageWithoutPhoto(chatId, prompt);
            commandStateStore.setCurrentCommand(chatId, STATE_WAITING_INPUT);
            return;
        }

        // Второй шаг: получили ввод пользователя — генерируем план и завершаем
        if (STATE_WAITING_INPUT.equals(commandStateStore.getCurrentCommand(chatId))) {
            try {
                String plan = aiAssistantService.generateTrainingProgram(text);
                sender.sendMessageWithoutPhoto(chatId, plan);
            } catch (Exception e) {
                sender.sendMessageWithoutPhoto(chatId, "Не удалось создать программу тренировки. Попробуйте ещё раз позже.");
            } finally {
                commandStateStore.clearCurrentCommand(chatId);
            }
        }
    }
}