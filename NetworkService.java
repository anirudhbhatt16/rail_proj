package com.railops.network;

import com.railops.common.ResourceNotFoundException;
import com.railops.network.dto.NetworkDto;
import com.railops.network.dto.NetworkMapResponse;
import com.railops.network.dto.TrainDto;
import com.railops.station.StationRepository;
import com.railops.track.TrackRepository;
import com.railops.train.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetworkService {

    private final NetworkRepository networkRepository;
    private final StationRepository stationRepository;
    private final TrackRepository trackRepository;
    private final TrainRepository trainRepository;

    public NetworkService(NetworkRepository networkRepository,
                         StationRepository stationRepository,
                         TrackRepository trackRepository,
                         TrainRepository trainRepository) {
        this.networkRepository = networkRepository;
        this.stationRepository = stationRepository;
        this.trackRepository = trackRepository;
        this.trainRepository = trainRepository;
    }

    public List<NetworkDto> getAllNetworks() {
        return networkRepository.findAll().stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .map(NetworkMapper::toDto)
                .toList();
    }

    public NetworkMapResponse getNetworkMap(Long networkId) {
        Network network = networkRepository.findById(networkId)
                .orElseThrow(() -> new ResourceNotFoundException("Network not found: " + networkId));

        return NetworkMapper.toMapResponse(
                network,
                stationRepository.findAllByNetworkId(networkId),
                trackRepository.findAllByNetworkId(networkId)
        );
    }

    public List<TrainDto> getNetworkTrains(Long networkId) {
        Network network = networkRepository.findById(networkId)
                .orElseThrow(() -> new ResourceNotFoundException("Network not found: " + networkId));

        return trainRepository.findAllByNetworkIdOrderByPriorityDesc(networkId).stream()
                .map(NetworkMapper::toTrainDto)
                .toList();
    }
}
