package com.sport.service.processors;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.entities.JointTraining;
import com.sport.service.services.NotificationCreatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JointTrainingProcessor {
    private final NotificationCreatorService notificationCreatorService;

    private final TelegramMessageSender sender;

    @Value("${telegram.secondAdminId}")
    private String secondAdminId;

    public void processRequestToApproveJointTraining(JointTraining jointTraining) {
        String requestToApprove = notificationCreatorService.createRequestToApproveJointTraining(jointTraining);

        InlineKeyboardMarkup markup = createKeyboardToChooseApprovingOptions(jointTraining.getId());

        SendMessage sendMessage = SendMessage.builder()
                .chatId(secondAdminId)
                .text(requestToApprove)
                .replyMarkup(markup)
                .build();
        sender.sendMessageWithoutPhoto(sendMessage);

        sender.sendMessageWithoutPhoto(jointTraining.getSubscriber().getId(),
                "Ваш запрос на создание/обновление совместной тренировки отправлен на проверку админу");
    }

    private InlineKeyboardMarkup createKeyboardToChooseApprovingOptions(Long jointTrainingId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton approve = new InlineKeyboardButton();
        approve.setText("✔️ Одобрить");
        approve.setCallbackData("APPROVE_JT:" + jointTrainingId);
        InlineKeyboardButton reject = new InlineKeyboardButton();
        reject.setText("❌ Отклонить");
        reject.setCallbackData("REJECT_JT:" + jointTrainingId);
        rows.add(List.of(approve, reject));
        markup.setKeyboard(rows);
        return markup;
    }

    public void notifyUserApproved(JointTraining jt) {
        sender.sendMessageWithoutPhoto(
                jt.getSubscriber().getId(),
                "🎉 Ваша тренировка одобрена!\n" +
                        "Название: " + jt.getTitle()
        );
    }

    public void notifyUserRejected(JointTraining jt, String reason) {
        sender.sendMessageWithoutPhoto(
                jt.getSubscriber().getId(),
                "❌ Ваша тренировка отклонена\nПричина: " + reason
        );
    }
}