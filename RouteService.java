package com.railops.route;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final RouteSegmentRepository routeSegmentRepository;

    public RouteService(RouteRepository routeRepository, RouteSegmentRepository routeSegmentRepository) {
        this.routeRepository = routeRepository;
        this.routeSegmentRepository = routeSegmentRepository;
    }

    public List<Route> findRoutesForNetwork(Long networkId) {
        return routeRepository.findAll().stream()
                .filter(route -> route.getNetwork() != null && route.getNetwork().getId().equals(networkId))
                .toList();
    }

    public List<RouteSegment> getSegmentsForRoute(Long routeId) {
        return routeSegmentRepository.findAllByRouteIdOrderBySequenceNumberAsc(routeId);
    }
}
