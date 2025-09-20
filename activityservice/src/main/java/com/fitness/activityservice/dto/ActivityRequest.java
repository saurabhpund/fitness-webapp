package com.fitness.activityservice.dto;

import com.fitness.activityservice.enums.ActivityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
public class ActivityRequest {
    private ActivityType type;
    private String userId;
    private Integer duration;
    private Integer caloriesBurned;
    private LocalDateTime startTime;
    private Map<String, Object> additionalMetrics;
}
