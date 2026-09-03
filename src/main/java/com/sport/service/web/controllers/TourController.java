package com.sport.service.web.controllers;

import com.sport.service.services.TourService;
import com.sport.service.services.TourRecordService;
import com.sport.service.web.models.tooltip.TooltipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("api/tours")
@RequiredArgsConstructor
@Slf4j
public class TourController {
    private final TourService tourService;
    private final TourRecordService tourRecordService;

    @GetMapping("/{route}/steps")
    public List<TooltipResponse> getTourSteps(
        @PathVariable String route
    ) {
        log.info("TourController.getTourSteps: route={}", route);
        String normalized = normalizeRoute(route);
        var result = tourService.getStepsByRoute(normalized);
        log.info("TourController.getTourSteps: returning {} steps for route={}", result.size(), normalized);
        return result;
    }

    @GetMapping("/steps")
    public List<TooltipResponse> getTourSteps() {
        return getTourSteps("/");
    }

    @GetMapping("/{route}/shown")
    public boolean isTourShown(
        Authentication auth,
        @PathVariable String route
    ) {
        log.info("TourController.isTourShown: route={}", route);
        String normalized = normalizeRoute(route);
        Long userId = (Long) auth.getPrincipal();
        log.info("TourController.isTourShown: userId={}, route={}", userId, normalized);
        boolean shown = tourService.isTourShown(userId, normalized);
        log.info("TourController.isTourShown: userId={}, route={}, result={}", userId, normalized, shown);
        return shown;
    }

    @GetMapping("/shown")
    public boolean isTourShown(Authentication auth) {
        return isTourShown(auth, "/");
    }

    @PostMapping("/record")
    public void recordTourShown(
        Authentication auth,
        @RequestParam String route
    ) {
        log.info("TourController.recordTourShown: route={}", route);
        String normalized = normalizeRoute(route);
        Long userId = (Long) auth.getPrincipal();
        log.info("TourController.recordTourShown: userId={}, route={}", userId, normalized);
        tourRecordService.recordTourShown(userId, normalized);
    }

    private String normalizeRoute(String route) {
        if (route == null || route.isEmpty()) return "/";
        return route.startsWith("/") ? route : "/" + route;
    }
}
