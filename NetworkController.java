package com.railops.network;

import com.railops.network.dto.NetworkDto;
import com.railops.network.dto.NetworkMapResponse;
import com.railops.network.dto.TrainDto;
import com.railops.route.dto.RouteOptimizationRequest;
import com.railops.route.dto.RouteOptimizationResponse;
import com.railops.route.service.RouteOptimizationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NetworkController {

    private final NetworkService networkService;
    private final RouteOptimizationService routeOptimizationService;

    public NetworkController(NetworkService networkService,
                            RouteOptimizationService routeOptimizationService) {
        this.networkService = networkService;
        this.routeOptimizationService = routeOptimizationService;
    }

    @GetMapping("/networks")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<List<NetworkDto>> getNetworks() {
        return ResponseEntity.ok(networkService.getAllNetworks());
    }

    @GetMapping("/networks/{id}/map")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<NetworkMapResponse> getNetworkMap(@PathVariable("id") Long id) {
        return ResponseEntity.ok(networkService.getNetworkMap(id));
    }

    @GetMapping("/networks/{id}/trains")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','VIEWER')")
    public ResponseEntity<List<TrainDto>> getNetworkTrains(@PathVariable("id") Long id) {
        return ResponseEntity.ok(networkService.getNetworkTrains(id));
    }

    @PostMapping("/networks/{id}/routes/optimize")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<RouteOptimizationResponse> optimizeRoute(
            @PathVariable("id") Long networkId,
            @Valid @RequestBody RouteOptimizationRequest request) {
        RouteOptimizationResponse response = routeOptimizationService.optimizeRoute(networkId, request.getSourceStationId(), request.getDestinationStationId());
        return ResponseEntity.ok(response);
    }
}
