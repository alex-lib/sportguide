package com.sport.service.services;

import com.sport.service.entities.Tour;
import com.sport.service.entities.Tooltip;
import com.sport.service.repositories.TourRepository;
import com.sport.service.repositories.TooltipRepository;
import com.sport.service.web.models.tooltip.TooltipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {

    private final TourRepository tourRepository;
    private final TooltipRepository tooltipRepository;
    private final TourRecordService tourRecordService;

    @Transactional(readOnly = true)
    public List<TooltipResponse> getStepsByRoute(String route) {
        Tour tour = tourRepository.findByRoute(route).orElse(null);
        if (tour == null) {
            return List.of();
        }
        return tooltipRepository.findByTourIdOrderByPositionAsc(tour.getId()).stream()
            .map(this::toResponse)
            .toList();
    }

    private TooltipResponse toResponse(Tooltip tooltip) {
        return TooltipResponse.builder()
            .target(tooltip.getTarget())
            .content(tooltip.getContent())
            .placement(tooltip.getPlacement())
            .isPrimary(tooltip.getIsPrimary())
            .build();
    }

    @Transactional(readOnly = true)
    public boolean isTourShown(Long subscriberId, String route) {
        return tourRecordService.hasTourBeenShown(subscriberId, route);
    }
}
