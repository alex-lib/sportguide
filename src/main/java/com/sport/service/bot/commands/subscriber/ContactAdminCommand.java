package com.sport.service.bot.commands.subscriber;
import com.sport.service.events.EventContactAdmin;
import com.sport.service.sessions.CommandStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactAdminCommand implements IBotCommand {

	private final ApplicationEventPublisher eventPublisher;

	private final CommandStateStore commandStateStore;

	@Override
	public String getCommandIdentifier() {
		return "contact_admin";
	}

	@Override
	public String getDescription() {
		return "Let subscriber to contact admin";
	}

	@Override
	public void processMessage(AbsSender absSender, Message message, String[] arguments) {
		User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        log.info("Call command contact_admin by userId={}, username={}", userId, user.getUserName());
		SendMessage answer = new SendMessage();
        answer.setChatId(chatId);

        commandStateStore.setCurrentCommand(userId, "contact_admin");
		answer.setText("📩 Напишите ваше предложение:");
		try {
			absSender.execute(answer);
		} catch (TelegramApiException e) {
			log.error("Error occurred in /contact_admin command", e);
		}
	}

	public void processTextInput(AbsSender absSender, Message message) {
		Long chatId = message.getChatId();
		Long userId = message.getFrom().getId();
		User user = message.getFrom();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

		if (!"contact_admin".equals(commandStateStore.getCurrentCommand(userId))) {
			return;
		}

		String text = message.getText();
		log.info("Received text: {}", text);
		eventPublisher.publishEvent(new EventContactAdmin(text, user));
		commandStateStore.clearCurrentCommand(userId);
        answer.setText("Сообщение отправлено админу ✅");
		try {
			absSender.execute(answer);
		} catch (Exception e) {
			log.error("Error processing text input", e);
            sendErrorMessage(absSender, chatId);
		}
	}

    private void sendErrorMessage(AbsSender absSender, Long chatId) {
		try {
			SendMessage errorMsg = new SendMessage();
			errorMsg.setChatId(chatId.toString());
            errorMsg.setText("Ошибка при обработке ввода. Попробуйте еще раз \uD83D\uDD04");
			absSender.execute(errorMsg);
		} catch (Exception e) {
			log.error("Error sending error message", e);
		}
	}
}