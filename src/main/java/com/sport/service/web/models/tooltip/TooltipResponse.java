package com.sport.service.web.models.tooltip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TooltipResponse {
    private String target;
    private String content;
    private String placement;
    private Boolean isPrimary;
}