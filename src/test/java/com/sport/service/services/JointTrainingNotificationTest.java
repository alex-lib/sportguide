package com.sport.service.services;

import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.entities.JointTraining;
import com.sport.service.entities.Subscriber;
import com.sport.service.entities.enums.jointtraining.ApprovalStatus;
import com.sport.service.mappers.jointtraining.JointTrainingMapper;
import com.sport.service.repositories.JointTrainingRepository;
import com.sport.service.store.notifications.Notification;
import com.sport.service.web.models.joint_training.CreateJointTrainingRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JointTrainingNotificationTest {
    private final JointTrainingRepository repository = mock(JointTrainingRepository.class);
    private final JointTrainingMapper mapper = mock(JointTrainingMapper.class);
    private final SubscriberService subscribers = mock(SubscriberService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final NotificationCreatorService creator = mock(NotificationCreatorService.class);
    private final JointTrainingService service = new JointTrainingService(repository, mapper, subscribers,
            creator, mock(TelegramMessageSender.class), events);

    @Test
    void pendingCreationDoesNotNotifySubscribersBeforeModeration() {
        Subscriber subscriber = Subscriber.builder().id(10L).build();
        CreateJointTrainingRequest request = new CreateJointTrainingRequest();
        JointTraining training = training();
        ReflectionTestUtils.setField(service, "secondAdminId", "20");
        when(creator.createRequestToApproveJointTraining(training)).thenReturn("Запрос на проверку");
        when(subscribers.findById(10L)).thenReturn(subscriber);
        when(mapper.createJointTrainingRequestToJointTraining(request, subscriber)).thenReturn(training);
        when(repository.save(any(JointTraining.class))).thenAnswer(call -> call.getArgument(0));
        service.createJointTraining(request, 10L);
        assertEquals(ApprovalStatus.PENDING, training.getApprovalStatus());
        verifyNoInteractions(events);
    }

    @Test
    void firstApprovalPublishesExactlyOneAnnouncement() {
        JointTraining training = training();
        when(repository.findById(42L)).thenReturn(Optional.of(training));
        service.approveJointTraining(42L);
        service.approveJointTraining(42L);
        ArgumentCaptor<Notification> notification = ArgumentCaptor.forClass(Notification.class);
        verify(events).publishEvent(notification.capture());
        assertEquals(Notification.NotificationType.JOINT_TRAINING, notification.getValue().getType());
        assertTrue(notification.getValue().getMessage().contains("Бег &lt;в парке&gt;"));
    }

    @Test
    void reapprovalAfterEditingDoesNotAnnounceOldTrainingAsNew() {
        JointTraining training = training();
        training.setApprovedAt(LocalDateTime.now().minusDays(1));
        when(repository.findById(42L)).thenReturn(Optional.of(training));
        service.approveJointTraining(42L);
        assertEquals(ApprovalStatus.APPROVED, training.getApprovalStatus());
        verifyNoInteractions(events);
    }

    @Test
    void rejectedTrainingIsNotAnnounced() {
        JointTraining training = training();
        when(repository.findById(42L)).thenReturn(Optional.of(training));
        service.rejectJointTraining(42L, "Причина");
        verifyNoInteractions(events);
    }

    private JointTraining training() {
        return JointTraining.builder().id(42L).title("Бег <в парке>")
                .subscriber(Subscriber.builder().id(10L).build()).build();
    }
}
