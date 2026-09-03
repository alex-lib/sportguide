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
        return tourService.getStepsByRoute(route);
    }

    @GetMapping("/{route}/shown")
    public boolean isTourShown(
        Authentication auth,
        @PathVariable String route
    ) {
        Long userId = (Long) auth.getPrincipal();
        return tourService.isTourShown(userId, route);
    }

    @PostMapping("/record")
    public void recordTourShown(
        Authentication auth,
        @RequestParam String route
    ) {
        Long userId = (Long) auth.getPrincipal();
        tourRecordService.recordTourShown(userId, route);
    }
}