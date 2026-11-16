package com.railops.deadlock;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class WaitForGraph {

    private final Map<Long, Set<Long>> edges = new LinkedHashMap<>();

    public void addEdge(Long waitingTrainId, Long holdingTrainId) {
        if (waitingTrainId == null || holdingTrainId == null || waitingTrainId.equals(holdingTrainId)) {
            return;
        }
        edges.computeIfAbsent(waitingTrainId, key -> new LinkedHashSet<>()).add(holdingTrainId);
    }

    public Map<Long, Set<Long>> asMap() {
        Map<Long, Set<Long>> copy = new LinkedHashMap<>();
        edges.forEach((trainId, holders) -> copy.put(trainId, Collections.unmodifiableSet(new LinkedHashSet<>(holders))));
        return Collections.unmodifiableMap(copy);
    }
}
