package com.railops.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.LinkedHashMap;
import java.util.Map;

public class AiPredictionRequest {

    @NotBlank(message = "trainType is required")
    @Pattern(regexp = "^(passenger|freight|commuter)$", message = "trainType must be passenger, freight or commuter")
    @JsonProperty("train_type")
    private String trainType;

    @NotNull(message = "priority is required")
    @Min(value = 1, message = "priority must be between 1 and 5")
    @Max(value = 5, message = "priority must be between 1 and 5")
    private Integer priority;

    @NotNull(message = "routeLengthKm is required")
    @Positive(message = "routeLengthKm must be greater than 0")
    @JsonProperty("route_length_km")
    private Double routeLengthKm;

    @NotNull(message = "scheduledHour is required")
    @Min(value = 0, message = "scheduledHour must be between 0 and 23")
    @Max(value = 23, message = "scheduledHour must be between 0 and 23")
    @JsonProperty("scheduled_hour")
    private Integer scheduledHour;

    @NotNull(message = "dayOfWeek is required")
    @Min(value = 0, message = "dayOfWeek must be between 0 and 6")
    @Max(value = 6, message = "dayOfWeek must be between 0 and 6")
    @JsonProperty("day_of_week")
    private Integer dayOfWeek;

    @NotNull(message = "congestionLevel is required")
    @Min(value = 0, message = "congestionLevel must be between 0 and 4")
    @Max(value = 4, message = "congestionLevel must be between 0 and 4")
    @JsonProperty("congestion_level")
    private Integer congestionLevel;

    @NotNull(message = "weatherCode is required")
    @Min(value = 0, message = "weatherCode must be between 0 and 4")
    @Max(value = 4, message = "weatherCode must be between 0 and 4")
    @JsonProperty("weather_code")
    private Integer weatherCode;

    @NotBlank(message = "trackCondition is required")
    @Pattern(regexp = "^(good|fair|poor)$", message = "trackCondition must be good, fair or poor")
    @JsonProperty("track_condition")
    private String trackCondition;

    @NotNull(message = "previousDelayMinutes is required")
    @Min(value = 0, message = "previousDelayMinutes must be non-negative")
    @JsonProperty("previous_delay_minutes")
    private Double previousDelayMinutes;

    public AiPredictionRequest() {
    }

    public Map<String, Object> toFeatureMap() {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("train_type", trainType);
        features.put("priority", priority);
        features.put("route_length_km", routeLengthKm);
        features.put("scheduled_hour", scheduledHour);
        features.put("day_of_week", dayOfWeek);
        features.put("congestion_level", congestionLevel);
        features.put("weather_code", weatherCode);
        features.put("track_condition", trackCondition);
        features.put("previous_delay_minutes", previousDelayMinutes);
        return features;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getRouteLengthKm() {
        return routeLengthKm;
    }

    public void setRouteLengthKm(Double routeLengthKm) {
        this.routeLengthKm = routeLengthKm;
    }

    public Integer getScheduledHour() {
        return scheduledHour;
    }

    public void setScheduledHour(Integer scheduledHour) {
        this.scheduledHour = scheduledHour;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getCongestionLevel() {
        return congestionLevel;
    }

    public void setCongestionLevel(Integer congestionLevel) {
        this.congestionLevel = congestionLevel;
    }

    public Integer getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(Integer weatherCode) {
        this.weatherCode = weatherCode;
    }

    public String getTrackCondition() {
        return trackCondition;
    }

    public void setTrackCondition(String trackCondition) {
        this.trackCondition = trackCondition;
    }

    public Double getPreviousDelayMinutes() {
        return previousDelayMinutes;
    }

    public void setPreviousDelayMinutes(Double previousDelayMinutes) {
        this.previousDelayMinutes = previousDelayMinutes;
    }
}
