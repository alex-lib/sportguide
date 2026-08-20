package com.sport.service.services;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.common.District;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.entities.enums.joint_training.ApprovalStatus;
import com.sport.service.entities.enums.subscriber.RoleType;
import com.sport.service.exceptions.NotFoundException;
import com.sport.service.mappers.joint_training.JointTrainingMapper;
import com.sport.service.repositories.JointTrainingRepository;
import com.sport.service.utils.BeanUtils;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import com.sport.service.web.models.joint_training.ListJointTrainingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sport.service.entities.enums.joint_training.ApprovalStatus.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class JointTrainingService {
    private final JointTrainingRepository jointTrainingRepository;
    private final JointTrainingMapper jointTrainingMapper;
    private final SubscriberService subscriberService;
    private final NotificationCreatorService notificationCreatorService;
    private final TelegramMessageSender sender;

    @Value("${telegram.secondAdminId}")
    private String secondAdminId;

    public ListJointTrainingResponse findAllJointTrainings(String districtStr, String dateStr, List<String> sportTypeRequest, String search) {
        log.info("findAll JointTrainings | district={}, date={}, sportTypes={}",
                districtStr, dateStr, sportTypeRequest);
        District district;
        if (districtStr == null || districtStr.isEmpty() || districtStr.equals("ALL_DISTRICTS")) {
            district = null;
        } else {
            district = District.valueOf(districtStr);
        }
        LocalDate date = dateStr != null && !dateStr.isEmpty() ? LocalDate.parse(dateStr) : null;
        List<SportType> sportTypes = null;
        if (sportTypeRequest != null && !sportTypeRequest.isEmpty()) {
            sportTypes = sportTypeRequest.stream()
                    .map(SportType::valueOf)
                    .collect(Collectors.toList());
        }

        var result = jointTrainingRepository.findWithFilters(district, date, sportTypes, search);
        log.info("findAll JointTrainings | resolved district={}, date={}, sportTypes={}, search={} | found={} trainings",
                district, date, sportTypes, search, result.size());
        return jointTrainingMapper.jointTrainingListToListJointTrainingResponse(result);
    }

    @Transactional
    public void createJointTraining(CreateJointTrainingRequest request, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);
        JointTraining jointTraining =
                jointTrainingMapper.createJointTrainingRequestToJointTraining(request, subscriber);

        jointTraining.setApprovalStatus(PENDING);
        jointTraining = jointTrainingRepository.save(jointTraining);
        jointTraining.setSubscriber(subscriber);

        processJointTrainingApprovalRequest(jointTraining);
    }

    @Transactional
    public void updateJointTrainingById(CreateJointTrainingRequest request, Long id, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);

        JointTraining jointTraining = jointTrainingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("JointTraining with id " + id + " not found"));

        if (!jointTraining.getSubscriber().getId().equals(userId)
                && subscriber.getRole() != RoleType.ADMIN) {
            throw new AccessDeniedException("You don't have permission to update this joint training");
        }

        JointTraining updatedJointTraining =
                jointTrainingMapper.createJointTrainingRequestToJointTraining(request, subscriber);

        BeanUtils.copyNonNullProperties(updatedJointTraining, jointTraining);

        jointTraining.setApprovalStatus(ApprovalStatus.PENDING);
        jointTrainingRepository.save(jointTraining);

        processJointTrainingApprovalRequest(jointTraining);
    }

    @Transactional
    public void deleteJointTrainingById(Long id, Long userId) {
        Subscriber subscriber = subscriberService.findById(userId);

        JointTraining jointTraining =
                jointTrainingRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("JointTraining with id " + id + " not found"));

        if (!jointTraining.getSubscriber().getId().equals(userId)
                && subscriber.getRole() != RoleType.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this joint training");
        }

        jointTrainingRepository.deleteById(id);
    }

    @Transactional
    public void approveJointTraining(Long id) {
        JointTraining jt = jointTrainingRepository.findById(id)
                .orElseThrow();

        if (jt.getApprovalStatus() != ApprovalStatus.PENDING)
            return;

        jt.setApprovalStatus(ApprovalStatus.APPROVED);
        jt.setApprovedAt(LocalDateTime.now());
        jointTrainingRepository.save(jt);
        notifyUserApproved(jt);
    }

    @Transactional
    public void rejectJointTraining(Long id, String reason) {
        JointTraining jt = jointTrainingRepository.findById(id)
                .orElseThrow();

        if (jt.getApprovalStatus() != ApprovalStatus.PENDING)
            return;

        jt.setApprovalStatus(ApprovalStatus.REJECTED);
        jt.setRejectionReason(reason);
        jointTrainingRepository.save(jt);
        notifyUserRejected(jt, reason);
    }

    private void processJointTrainingApprovalRequest(JointTraining jointTraining) {
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

    private void notifyUserApproved(JointTraining jt) {
        sender.sendMessageWithoutPhoto(
                jt.getSubscriber().getId(),
                "🎉 Ваша тренировка одобрена!\n" +
                        "Название: " + jt.getTitle()
        );
    }

    private void notifyUserRejected(JointTraining jt, String reason) {
        sender.sendMessageWithoutPhoto(
                jt.getSubscriber().getId(),
                "❌ Ваша тренировка отклонена\nПричина: " + reason
        );
    }
}
