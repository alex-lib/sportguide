package com.sport.service.bot.commands.admin;

import com.sport.service.aop.aspects.AdminOnlyAspect;
import com.sport.service.bot.SportGuideBot;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.UtilMethods;
import com.sport.service.bot.commands.menu.AdminMenu;
import com.sport.service.bot.commands.menu.SubscriberMenu;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.MenuConstants;
import com.sport.service.configurations.RedisBotCommandSessionConfiguration;
import com.sport.service.entities.Subscriber;
import com.sport.service.services.CoachService;
import com.sport.service.services.JointTrainingService;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.RedisCrud;
import com.sport.service.store.commands.sessions.CoachSession;
import com.sport.service.store.commands.sessions.CoachSession.Step;
import com.sport.service.store.commands.sessions.JointTrainingRejectingSession;
import com.sport.service.web.models.coach.CoachRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

class CreateCoachCommandTest {
    private static final long ADMIN_ID = 10L;
    private static final long CHAT_ID = 20L;
    private final Map<String, Object> data = new HashMap<>();
    private final RedisCrud store = mock(RedisCrud.class);
    private final CoachSession sessions = new CoachSession(store);
    private final CommandStateStore commands = new CommandStateStore(store);
    private final CoachService coaches = mock(CoachService.class);
    private final SubscriberService subscribers = mock(SubscriberService.class);
    private final PlaceService places = mock(PlaceService.class);
    private final TelegramMessageSender sender = mock(TelegramMessageSender.class);
    private final AbsSender telegram = mock(AbsSender.class);
    private final CreateCoachCommand command = new CreateCoachCommand(sessions, coaches, subscribers, places, commands, sender);

    @BeforeEach
    void setUp() {
        when(store.get(anyString(), any())).thenAnswer(call -> data.get(call.getArgument(0)));
        doAnswer(call -> {
            data.put(call.getArgument(0), call.getArgument(1));
            return null;
        }).when(store).put(anyString(), any(), anyLong());
        doAnswer(call -> data.remove(call.getArgument(0))).when(store).delete(anyString());
        when(subscribers.checkIfAdmin(ADMIN_ID)).thenReturn(true);
        when(subscribers.findByUsername("@coach_name"))
                .thenReturn(Optional.of(Subscriber.builder().id(99L).username("coach_name").build()));
    }

    @Test
    void menuButtonStartsRegisteredCommandAndIsAbsentFromSubscriberMenu() {
        SendMessage adminMenu = new SendMessage();
        new AdminMenu(adminMenu).getAdminMenu();
        assertTrue(hasCoachButton(adminMenu));
        SendMessage subscriberMenu = new SendMessage();
        new SubscriberMenu(subscriberMenu).getSubscriberMenu();
        assertFalse(hasCoachButton(subscriberMenu));

        SportGuideBot bot = new SportGuideBot("test", "test", List.of(command), commands,
                mock(JointTrainingService.class), mock(JointTrainingRejectingSession.class), sender);
        ReflectionTestUtils.setField(bot, "secondAdminId", "10");
        Update update = new Update();
        update.setMessage(message(MenuConstants.CREATE_COACH));
        bot.processNonCommandUpdate(update);
        assertEquals(CommandsConstants.CREATE_COACH, commands.getCurrentCommand(ADMIN_ID));
        assertEquals(Step.SUBSCRIBER, sessions.getIfExists(ADMIN_ID).getStep());
    }

    @Test
    void completeFlowSavesAllFieldsAndPhotoAndClearsState() {
        completeForm();
        byte[] bytes = {1, 2, 3};
        try (var download = mockStatic(UtilMethods.class)) {
            download.when(() -> UtilMethods.downloadPhoto(telegram, "photo", null)).thenReturn(bytes);
            command.processPhotoInput(telegram, photo());
        }
        ArgumentCaptor<CoachRequest> request = ArgumentCaptor.forClass(CoachRequest.class);
        verify(coaches).createCoachFromBot(request.capture(), eq(bytes));
        CoachRequest saved = request.getValue();
        assertAll(
                () -> assertEquals(99L, saved.getSubscriberId()),
                () -> assertEquals("Иван Иванов", saved.getName()),
                () -> assertEquals(List.of("Фитнес", "Бокс"), saved.getSportTypes()),
                () -> assertEquals("Персональные тренировки", saved.getDescription()),
                () -> assertEquals(30, saved.getAge()),
                () -> assertEquals("Мужчина", saved.getSex()),
                () -> assertEquals(5, saved.getYearsOfExperience()),
                () -> assertEquals("Физкультурный институт", saved.getEducation()),
                () -> assertEquals("+79991234567", saved.getPhoneNumber()),
                () -> assertEquals(List.of("Зал, центр", "Стадион"), saved.getWorkPlacesNames()),
                () -> assertEquals((short) 3, saved.getMonthsForSubscriptionToBeCoach()),
                () -> assertTrue(saved.getShowInWeb()),
                () -> assertTrue(saved.getTrainingProgramsTitles().isEmpty()));
        assertNull(sessions.getIfExists(ADMIN_ID));
        assertNull(commands.getCurrentCommand(ADMIN_ID));
        verify(sender).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_CREATED);
    }

    @Test
    void invalidInputKeepsCurrentStepAndDoesNotSave() {
        start();
        text("not a username");
        text("@unknown_coach");
        assertEquals(Step.SUBSCRIBER, step());
        text("@coach_name");
        text(" ");
        assertEquals(Step.NAME, step());
        text("Имя");
        callback("coach:done");
        assertEquals(Step.SPORTS, step());
        callback("coach:sport:FITNESS");
        callback("coach:sport:FITNESS");
        assertTrue(sessions.getIfExists(ADMIN_ID).getRequest().getSportTypes().isEmpty());
        callback("coach:sport:FITNESS");
        callback("coach:done");
        text("Описание");
        text("17");
        assertEquals(Step.AGE, step());
        text("30");
        callback("coach:male");
        text("-1");
        assertEquals(Step.EXPERIENCE, step());
        text("5");
        text("Образование");
        text("bad phone");
        assertEquals(Step.PHONE, step());
        text("-");
        assertNull(sessions.getIfExists(ADMIN_ID).getRequest().getPhoneNumber());
        text("Несуществующий зал");
        assertEquals(Step.WORK_PLACES, step());
        text("-");
        text("0");
        text("32768");
        assertEquals(Step.MONTHS, step());
        verifyNoInteractions(coaches);
    }

    @Test
    void prematurePhotoAndUnrelatedCallbackCannotSkipSteps() {
        start();
        command.processPhotoInput(telegram, photo());
        callback("coach:show");
        assertEquals(Step.SUBSCRIBER, step());
        verifyNoInteractions(coaches, telegram);
    }

    @Test
    void cancelClearsDraftWithoutCreatingCoach() {
        start();
        text("/cancel");
        assertNull(sessions.getIfExists(ADMIN_ID));
        assertNull(commands.getCurrentCommand(ADMIN_ID));
        verifyNoInteractions(coaches);
    }

    @Test
    void expiredSessionClearsCommand() {
        start();
        sessions.clear(ADMIN_ID);
        text("@coach_name");
        assertNull(commands.getCurrentCommand(ADMIN_ID));
        verifyNoInteractions(coaches);
    }

    @Test
    void differentChatAndRevokedAdminCannotModifyDraft() {
        start();
        Message otherChat = message("@coach_name");
        otherChat.getChat().setId(30L);
        command.processTextInput(telegram, otherChat);
        assertEquals(Step.SUBSCRIBER, step());
        when(subscribers.checkIfAdmin(ADMIN_ID)).thenReturn(false);
        text("@coach_name");
        callback("coach:show");
        command.processPhotoInput(telegram, photo());
        assertEquals(Step.SUBSCRIBER, step());
        verifyNoInteractions(coaches);
    }

    @Test
    void nonAdminCannotStartCommandThroughAuthorizationAspect() {
        when(subscribers.checkIfAdmin(ADMIN_ID)).thenReturn(false);
        AspectJProxyFactory factory = new AspectJProxyFactory(command);
        factory.addAspect(new AdminOnlyAspect(subscribers));
        IBotCommand proxy = factory.getProxy();
        assertThrows(SecurityException.class, () -> proxy.processMessage(telegram, message(MenuConstants.CREATE_COACH), new String[0]));
        assertNull(sessions.getIfExists(ADMIN_ID));
    }

    @Test
    void failedSaveKeepsDraftForPhotoRetry() {
        completeForm();
        byte[] bytes = {1};
        doThrow(new IllegalStateException("Storage unavailable")).when(coaches).createCoachFromBot(any(), any());
        try (var download = mockStatic(UtilMethods.class)) {
            download.when(() -> UtilMethods.downloadPhoto(telegram, "photo", null)).thenReturn(bytes);
            command.processPhotoInput(telegram, photo());
        }
        assertEquals(Step.PHOTO, step());
        assertEquals(CommandsConstants.CREATE_COACH, commands.getCurrentCommand(ADMIN_ID));
        verify(sender, never()).sendMessageWithoutPhoto(CHAT_ID, CommandsConstants.COACH_CREATED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void draftRoundTripsThroughProductionRedisSerializer() {
        completeForm();
        var template = new RedisBotCommandSessionConfiguration()
                .botCommandSessionRedisTemplate(mock(RedisConnectionFactory.class));
        RedisSerializer<Object> serializer = (RedisSerializer<Object>) template.getValueSerializer();
        CoachSession.Draft restored = (CoachSession.Draft) serializer.deserialize(serializer.serialize(sessions.getIfExists(ADMIN_ID)));
        assertNotNull(restored);
        assertEquals(Step.PHOTO, restored.getStep());
        assertEquals(CHAT_ID, restored.getChatId());
        assertEquals(List.of("Фитнес", "Бокс"), restored.getRequest().getSportTypes());
        restored.getRequest().getSportTypes().add("Йога");
    }

    private boolean hasCoachButton(SendMessage menu) {
        return ((ReplyKeyboardMarkup) menu.getReplyMarkup()).getKeyboard().stream()
                .flatMap(List::stream).anyMatch(button -> MenuConstants.CREATE_COACH.equals(button.getText()));
    }

    private void completeForm() {
        start();
        text("@coach_name");
        text("Иван Иванов");
        callback("coach:sport:FITNESS");
        callback("coach:sport:BOXING");
        callback("coach:done");
        text("Персональные тренировки");
        text("30");
        callback("coach:male");
        text("5");
        text("Физкультурный институт");
        text("+79991234567");
        when(places.existsByName("Зал, центр")).thenReturn(true);
        when(places.existsByName("Стадион")).thenReturn(true);
        text("Зал, центр\nСтадион");
        text("3");
        callback("coach:show");
        assertEquals(Step.PHOTO, step());
    }

    private void start() {
        command.processMessage(telegram, message(MenuConstants.CREATE_COACH), new String[0]);
    }

    private Step step() {
        return sessions.getIfExists(ADMIN_ID).getStep();
    }

    private void text(String text) {
        command.processTextInput(telegram, message(text));
    }

    private void callback(String data) {
        CallbackQuery callback = new CallbackQuery();
        callback.setFrom(message("").getFrom());
        callback.setMessage(message(""));
        callback.setData(data);
        command.processCallback(telegram, callback);
    }

    private Message photo() {
        Message message = message(null);
        PhotoSize photo = new PhotoSize();
        photo.setFileId("photo");
        message.setPhoto(List.of(photo));
        return message;
    }

    private Message message(String text) {
        User user = new User();
        user.setId(ADMIN_ID);
        user.setIsBot(false);
        Chat chat = new Chat();
        chat.setId(CHAT_ID);
        Message message = new Message();
        message.setFrom(user);
        message.setChat(chat);
        message.setText(text);
        return message;
    }
}
