package com.railops.deadlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeadlockReport {

    private final List<Long> involvedTrains;
    private final List<Long> involvedResources;
    private final String cycleDescription;

    public DeadlockReport(List<Long> involvedTrains, List<Long> involvedResources, String cycleDescription) {
        this.involvedTrains = Collections.unmodifiableList(new ArrayList<>(involvedTrains));
        this.involvedResources = Collections.unmodifiableList(new ArrayList<>(involvedResources));
        this.cycleDescription = cycleDescription;
    }

    public List<Long> getInvolvedTrains() {
        return involvedTrains;
    }

    public List<Long> getInvolvedResources() {
        return involvedResources;
    }

    public String getCycleDescription() {
        return cycleDescription;
    }
}
