package com.railops.simulation;

import com.railops.event.SimulationEventDto;
import com.railops.resource.ResourceDto;
import com.railops.resource.ResourceRequestDto;

import java.util.List;

public class SimulationStateResponse {

    private final SimulationResponse simulation;
    private final List<ResourceDto> resources;
    private final List<ResourceRequestDto> requests;
    private final List<SimulationEventDto> events;

    public SimulationStateResponse(SimulationResponse simulation,
                                  List<ResourceDto> resources,
                                  List<ResourceRequestDto> requests,
                                  List<SimulationEventDto> events) {
        this.simulation = simulation;
        this.resources = resources;
        this.requests = requests;
        this.events = events;
    }

    public SimulationResponse getSimulation() {
        return simulation;
    }

    public List<ResourceDto> getResources() {
        return resources;
    }

    public List<ResourceRequestDto> getRequests() {
        return requests;
    }

    public List<SimulationEventDto> getEvents() {
        return events;
    }
}
