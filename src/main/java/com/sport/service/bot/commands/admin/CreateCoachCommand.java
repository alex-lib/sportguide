package com.sport.service.bot.commands.admin;

import com.sport.service.annotations.AdminOnly;
import com.sport.service.bot.TelegramMessageSender;
import com.sport.service.bot.commands.UtilMethods;
import com.sport.service.bot.commands.interfaces.CallbackProcessable;
import com.sport.service.bot.commands.interfaces.PhotoProcessable;
import com.sport.service.bot.commands.interfaces.TextProcessable;
import com.sport.service.bot.constants.CommandsConstants;
import com.sport.service.bot.constants.ErrorConstants;
import com.sport.service.entities.enums.common.SportType;
import com.sport.service.mappers.string.SportTypeStringMapper;
import com.sport.service.services.CoachService;
import com.sport.service.services.PlaceService;
import com.sport.service.services.SubscriberService;
import com.sport.service.store.commands.CommandStateStore;
import com.sport.service.store.commands.sessions.CoachSession;
import com.sport.service.store.commands.sessions.CoachSession.Draft;
import com.sport.service.store.commands.sessions.CoachSession.Step;
import com.sport.service.validators.PhoneNumberValidator;
import com.sport.service.web.models.coach.CoachRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateCoachCommand implements IBotCommand, TextProcessable, CallbackProcessable, PhotoProcessable {
    private static final String SPORT_PREFIX = "coach:sport:";
    private final CoachSession coachSession;
    private final CoachService coachService;
    private final SubscriberService subscriberService;
    private final PlaceService placeService;
    private final CommandStateStore commandStateStore;
    private final TelegramMessageSender sender;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getCommandIdentifier() {
        return CommandsConstants.CREATE_COACH;
    }

    @Override
    public String getDescription() {
        return CommandsConstants.CREATE_COACH_DESCRIPTION;
    }

    @Override
    @AdminOnly
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long userId = message.getFrom().getId();
        try {
            Draft draft = coachSession.createSession(userId, message.getChatId());
            commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
            prompt(draft);
        } catch (Exception e) {
            fail(message.getChatId(), userId, e);
        }
    }

    @Override
    public void processTextInput(AbsSender absSender, Message message) {
        Long userId = message.getFrom().getId();
        Draft draft = getSession(userId, message.getChatId());
        if (draft == null) return;

        String text = message.getText().trim();
        if ("/cancel".equals(text)) {
            clear(userId);
            sender.sendMessageWithoutPhoto(message.getChatId(), CommandsConstants.CREATE_COACH_CANCELLED);
            return;
        }

        try {
            acceptText(draft, text);
            saveAndPrompt(userId, draft);
        } catch (IllegalArgumentException e) {
            sender.sendMessageWithoutPhoto(message.getChatId(), e.getMessage());
            saveAndPrompt(userId, draft);
        } catch (Exception e) {
            fail(message.getChatId(), userId, e);
        }
    }

    private void acceptText(Draft draft, String text) {
        CoachRequest request = draft.getRequest();
        switch (draft.getStep()) {
            case SUBSCRIBER -> {
                var subscriber = subscriberService.findByUsername(text)
                        .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким никнеймом не найден в базе бота. Проверьте @username и попросите тренера нажать /start в этом боте, затем введите никнейм ещё раз."));
                request.setSubscriberId(subscriber.getId());
                draft.setStep(Step.NAME);
            }
            case NAME -> {
                request.setName(required(text));
                draft.setStep(Step.SPORTS);
            }
            case DESCRIPTION -> {
                request.setDescription(required(text));
                draft.setStep(Step.AGE);
            }
            case AGE -> {
                request.setAge(number(text, 18, 120));
                draft.setStep(Step.SEX);
            }
            case EXPERIENCE -> {
                request.setYearsOfExperience(number(text, 0, request.getAge()));
                draft.setStep(Step.EDUCATION);
            }
            case EDUCATION -> {
                request.setEducation(required(text));
                draft.setStep(Step.PHONE);
            }
            case PHONE -> {
                String phone = "-".equals(text) ? null : text;
                if (text.isBlank() || !new PhoneNumberValidator().isValid(phone, null)) {
                    throw new IllegalArgumentException("Введите телефон в формате +79991234567 или '-' без телефона.");
                }
                request.setPhoneNumber(phone);
                draft.setStep(Step.WORK_PLACES);
            }
            case WORK_PLACES -> {
                List<String> names = "-".equals(text) ? new ArrayList<>() :
                        new ArrayList<>(Arrays.stream(required(text).split("\\R"))
                                .map(String::trim).filter(name -> !name.isEmpty()).distinct().toList());
                if (names.stream().anyMatch(name -> !placeService.existsByName(name))) {
                    throw new IllegalArgumentException("Одно из мест не найдено. Проверьте точные названия или отправьте '-' без мест работы.");
                }
                request.setWorkPlacesNames(names);
                draft.setStep(Step.MONTHS);
            }
            case MONTHS -> {
                request.setMonthsForSubscriptionToBeCoach((short) number(text, 1, Short.MAX_VALUE));
                draft.setStep(Step.VISIBILITY);
            }
            default -> {
                // Repeat the current keyboard or photo prompt without advancing the draft.
            }
        }
    }

    @Override
    public void processCallback(AbsSender absSender, CallbackQuery callback) {
        Long userId = callback.getFrom().getId();
        Draft draft = getSession(userId, callback.getMessage().getChatId());
        if (draft == null) return;

        String data = callback.getData();
        try {
            CoachRequest request = draft.getRequest();
            if (draft.getStep() == Step.SPORTS && data != null && data.startsWith(SPORT_PREFIX)) {
                SportType sport = SportType.valueOf(data.substring(SPORT_PREFIX.length()));
                String name = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(List.of(sport)).getFirst();
                if (!request.getSportTypes().remove(name)) request.getSportTypes().add(name);
            } else if (draft.getStep() == Step.SPORTS && "coach:done".equals(data)) {
                if (request.getSportTypes().isEmpty()) {
                    sender.sendMessageWithoutPhoto(draft.getChatId(), "Выберите хотя бы один вид спорта.");
                } else {
                    draft.setStep(Step.DESCRIPTION);
                }
            } else if (draft.getStep() == Step.SEX && ("coach:male".equals(data) || "coach:female".equals(data))) {
                request.setSex("coach:male".equals(data) ? "Мужчина" : "Женщина");
                draft.setStep(Step.EXPERIENCE);
            } else if (draft.getStep() == Step.VISIBILITY && ("coach:show".equals(data) || "coach:hide".equals(data))) {
                request.setShowInWeb("coach:show".equals(data));
                draft.setStep(Step.PHOTO);
            }
            saveAndPrompt(userId, draft);
        } catch (IllegalArgumentException e) {
            prompt(draft);
        } catch (Exception e) {
            fail(draft.getChatId(), userId, e);
        }
    }

    @Override
    public void processPhotoInput(AbsSender absSender, Message message) {
        Long userId = message.getFrom().getId();
        Draft draft = getSession(userId, message.getChatId());
        if (draft == null) return;
        if (draft.getStep() != Step.PHOTO || !message.hasPhoto()) {
            prompt(draft);
            return;
        }

        try {
            List<PhotoSize> photos = message.getPhoto();
            byte[] photo = UtilMethods.downloadPhoto(absSender, photos.getLast().getFileId(), botToken);
            if (photo.length == 0) throw new IllegalArgumentException("Empty photo");
            coachService.createCoachFromBot(draft.getRequest(), photo);
        } catch (Exception e) {
            log.error("Failed to save coach for user {}", userId, e);
            sender.sendMessageWithoutPhoto(message.getChatId(), "Не удалось сохранить тренера. Отправьте фото ещё раз или отмените добавление: /cancel.");
            saveAndPrompt(userId, draft);
            return;
        }
        clear(userId);
        sender.sendMessageWithoutPhoto(message.getChatId(), CommandsConstants.COACH_CREATED);
    }

    private Draft getSession(Long userId, Long chatId) {
        if (!subscriberService.checkIfAdmin(userId)) return null;
        if (!getCommandIdentifier().equals(commandStateStore.getCurrentCommand(userId))) return null;
        Draft draft = coachSession.getIfExists(userId);
        if (draft == null) {
            commandStateStore.clearCurrentCommand(userId);
            sender.sendMessageWithoutPhoto(chatId, ErrorConstants.SESSION_EXPIRED);
            return null;
        }
        return chatId.equals(draft.getChatId()) ? draft : null;
    }

    private void saveAndPrompt(Long userId, Draft draft) {
        coachSession.save(userId, draft);
        commandStateStore.setCurrentCommand(userId, getCommandIdentifier());
        prompt(draft);
    }

    private void clear(Long userId) {
        coachSession.clear(userId);
        commandStateStore.clearCurrentCommand(userId);
    }

    private void fail(Long chatId, Long userId, Exception e) {
        log.error("Error in create_coach for user {}", userId, e);
        clear(userId);
        sender.sendMessageWithoutPhoto(chatId, ErrorConstants.ENTERING_ERROR);
    }

    private String required(String text) {
        if (text.isBlank() || "-".equals(text)) throw new IllegalArgumentException("Это поле обязательно для заполнения.");
        return text;
    }

    private int number(String text, int min, int max) {
        try {
            int value = Integer.parseInt(text);
            if (value >= min && value <= max) return value;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Введите целое число от " + min + " до " + max + ".");
    }

    private void prompt(Draft draft) {
        String text = switch (draft.getStep()) {
            case SUBSCRIBER -> "Введите Telegram-никнейм тренера, например @username. Можно без @. Тренер должен сначала нажать /start в этом боте; его Telegram ID будет определён автоматически.";
            case NAME -> "Введите имя и фамилию тренера:";
            case SPORTS -> "Выберите виды спорта (можно несколько), затем нажмите «Готово»:";
            case DESCRIPTION -> "Введите описание тренера:";
            case AGE -> "Введите возраст тренера (от 18 лет):";
            case SEX -> "Выберите пол тренера:";
            case EXPERIENCE -> "Введите стаж работы тренером в полных годах:";
            case EDUCATION -> "Введите образование тренера:";
            case PHONE -> "Введите телефон в формате +79991234567 или '-' без телефона:";
            case WORK_PLACES -> "Введите точные названия мест работы, каждое с новой строки, или '-' без мест работы:";
            case MONTHS -> "Введите срок размещения в месяцах (целое положительное число):";
            case VISIBILITY -> "Показывать тренера на сайте?";
            case PHOTO -> "Отправьте фото тренера. После загрузки анкета будет сохранена:";
        };
        SendMessage answer = new SendMessage();
        answer.setChatId(draft.getChatId());
        answer.setText(text + "\n\nДля отмены: /cancel");
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (draft.getStep() == Step.SPORTS) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (SportType sport : SportType.values()) {
                String name = SportTypeStringMapper.listSportTypeEnumToListSportTypeString(List.of(sport)).getFirst();
                row.add(button((draft.getRequest().getSportTypes().contains(name) ? "✅ " : "") + name, SPORT_PREFIX + sport.name()));
                if (row.size() == 2) {
                    rows.add(row);
                    row = new ArrayList<>();
                }
            }
            if (!row.isEmpty()) rows.add(row);
            rows.add(List.of(button("Готово", "coach:done")));
        } else if (draft.getStep() == Step.SEX) {
            rows.add(List.of(button("Мужчина", "coach:male"), button("Женщина", "coach:female")));
        } else if (draft.getStep() == Step.VISIBILITY) {
            rows.add(List.of(button("Да", "coach:show"), button("Нет", "coach:hide")));
        }
        if (!rows.isEmpty()) answer.setReplyMarkup(new InlineKeyboardMarkup(rows));
        sender.sendMessageWithoutPhoto(answer);
    }

    private InlineKeyboardButton button(String text, String data) {
        return InlineKeyboardButton.builder().text(text).callbackData(data).build();
    }
}
