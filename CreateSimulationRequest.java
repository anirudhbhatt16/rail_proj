package com.railops.simulation;

import com.railops.scheduling.SchedulingPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSimulationRequest {

    @NotNull(message = "networkId is required")
    private Long networkId;

    @NotBlank(message = "name is required")
    @Size(max = 150, message = "name must be at most 150 characters")
    private String name;

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code must be at most 50 characters")
    private String code;

    private SchedulingPolicy schedulingPolicy = SchedulingPolicy.FCFS;

    @Min(value = 1, message = "baseStepSeconds must be at least 1")
    private Integer baseStepSeconds = 60;

    @Min(value = 1, message = "speed must be at least 1")
    private Integer speed = 1;

    @Min(value = 1, message = "agingIntervalTicks must be at least 1")
    private Integer agingIntervalTicks = 5;

    public CreateSimulationRequest() {
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SchedulingPolicy getSchedulingPolicy() {
        return schedulingPolicy;
    }

    public void setSchedulingPolicy(SchedulingPolicy schedulingPolicy) {
        this.schedulingPolicy = schedulingPolicy;
    }

    public Integer getBaseStepSeconds() {
        return baseStepSeconds;
    }

    public void setBaseStepSeconds(Integer baseStepSeconds) {
        this.baseStepSeconds = baseStepSeconds;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getAgingIntervalTicks() {
        return agingIntervalTicks;
    }

    public void setAgingIntervalTicks(Integer agingIntervalTicks) {
        this.agingIntervalTicks = agingIntervalTicks;
    }
}
