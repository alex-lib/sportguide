package com.sport.service.aop.aspects;

import com.sport.service.aop.annotations.AdminOnly;
import com.sport.service.mappers.ButtonToCommandMapper;
import com.sport.service.services.SubscriberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.NoSuchElementException;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminOnlyAspect {
    private final SubscriberService subscriberService;

    @Before("@annotation(adminOnly)")
    public void checkAdmin(JoinPoint joinPoint, AdminOnly adminOnly) {
        Object[] args = joinPoint.getArgs();
        Long userId = null;
        Message message = null;
        String command = null;

        for (Object arg : args) {
            if (arg instanceof Message) {
                message = (Message) arg;
                command = ButtonToCommandMapper.mapButtonToCommand(message.getText());
                User user = message.getFrom();
                userId = user.getId();
                break;
            }
        }

        if (userId == null) {
            log.warn("AdminOnly annotation used but no Message(telegram package) type variable found in method: {}",
                    joinPoint.getSignature().getName());
            throw new NoSuchElementException("AdminOnly annotation used but no Message(telegram package) type variable found in method");
        }

        if (!subscriberService.checkIfAdmin(userId)) {
            log.warn("Non-admin user with id: {}, attempted to access admin command: {}",
                    userId, command);
            throw new SecurityException("There are no admin rights to use specific command");
        }

        log.debug("Admin authorization verified for user {} accessing command: {}",
                userId, command);
    }
}