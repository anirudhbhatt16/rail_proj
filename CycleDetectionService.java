package com.railops.deadlock;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CycleDetectionService {

    public List<List<Long>> detectCycles(Map<Long, Set<Long>> waitForGraph) {
        Map<Long, Integer> state = new HashMap<>();
        Deque<Long> path = new ArrayDeque<>();
        Set<List<Long>> cycles = new LinkedHashSet<>();

        for (Long trainId : waitForGraph.keySet()) {
            detectCyclesFromNode(trainId, waitForGraph, state, path, cycles);
        }

        return cycles.stream()
                .sorted(Comparator.comparingLong(cycle -> cycle.get(0)))
                .toList();
    }

    private void detectCyclesFromNode(Long trainId,
                                     Map<Long, Set<Long>> waitForGraph,
                                     Map<Long, Integer> state,
                                     Deque<Long> path,
                                     Set<List<Long>> cycles) {
        state.put(trainId, 1);
        path.addLast(trainId);

        for (Long nextTrainId : waitForGraph.getOrDefault(trainId, Set.of())) {
            if (!waitForGraph.containsKey(nextTrainId) && waitForGraph.values().stream().noneMatch(holders -> holders.contains(nextTrainId))) {
                continue;
            }

            Integer nextState = state.get(nextTrainId);
            if (nextState == null) {
                detectCyclesFromNode(nextTrainId, waitForGraph, state, path, cycles);
            } else if (nextState == 1) {
                List<Long> cycle = new ArrayList<>();
                List<Long> currentPath = new ArrayList<>(path);
                int startIndex = currentPath.indexOf(nextTrainId);
                if (startIndex >= 0) {
                    cycle.addAll(currentPath.subList(startIndex, currentPath.size()));
                    cycle.add(nextTrainId);
                    cycles.add(canonicalize(cycle));
                }
            }
        }

        path.removeLast();
        state.put(trainId, 2);
    }

    private List<Long> canonicalize(List<Long> cycle) {
        if (cycle.size() <= 1) {
            return cycle;
        }

        int bestIndex = 0;
        for (int i = 1; i < cycle.size() - 1; i++) {
            if (cycle.get(i) < cycle.get(bestIndex)) {
                bestIndex = i;
            }
        }

        List<Long> rotated = new ArrayList<>();
        for (int i = 0; i < cycle.size() - 1; i++) {
            rotated.add(cycle.get((bestIndex + i) % (cycle.size() - 1)));
        }
        rotated.add(rotated.get(0));
        return rotated;
    }
}
