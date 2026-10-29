package com.sport.service.store.commands.sessions;

import com.sport.service.store.commands.RedisCrud;
import com.sport.service.web.models.coach.CoachRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class CoachSession {
    private static final String KEY_PREFIX = "session:coach:";
    private static final long TTL_SECONDS = 300;
    private final RedisCrud store;

    public Draft createSession(Long userId, Long chatId) {
        Draft draft = new Draft();
        draft.setChatId(chatId);
        draft.getRequest().setSportTypes(new ArrayList<>());
        draft.getRequest().setWorkPlacesNames(new ArrayList<>());
        draft.getRequest().setTrainingProgramsTitles(new ArrayList<>());
        save(userId, draft);
        return draft;
    }

    public Draft getIfExists(Long userId) {
        return store.get(KEY_PREFIX + userId, Draft.class);
    }

    public void save(Long userId, Draft draft) {
        store.put(KEY_PREFIX + userId, draft, TTL_SECONDS);
    }

    public void clear(Long userId) {
        store.delete(KEY_PREFIX + userId);
    }

    public enum Step {
        SUBSCRIBER, NAME, SPORTS, DESCRIPTION, AGE, SEX, EXPERIENCE,
        EDUCATION, PHONE, WORK_PLACES, MONTHS, VISIBILITY, PHOTO
    }

    @Getter
    @Setter
    public static class Draft {
        private Long chatId;
        private Step step = Step.SUBSCRIBER;
        private CoachRequest request = new CoachRequest();
    }
}
