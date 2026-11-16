package com.railops.network;

import com.railops.network.dto.NetworkDto;
import com.railops.network.dto.NetworkMapResponse;
import com.railops.network.dto.StationDto;
import com.railops.network.dto.TrackDto;
import com.railops.network.dto.TrainDto;
import com.railops.station.Station;
import com.railops.track.Track;
import com.railops.train.Train;

import java.util.List;

public final class NetworkMapper {

    private NetworkMapper() {
    }

    public static NetworkDto toDto(Network network) {
        if (network == null) {
            return null;
        }

        return new NetworkDto(
                network.getId(),
                network.getName(),
                network.getCode(),
                network.getDescription()
        );
    }

    public static StationDto toStationDto(Station station) {
        if (station == null) {
            return null;
        }

        return new StationDto(
                station.getId(),
                station.getCode(),
                station.getName(),
                station.getLatitude(),
                station.getLongitude()
        );
    }

    public static TrackDto toTrackDto(Track track) {
        if (track == null) {
            return null;
        }

        return new TrackDto(
                track.getId(),
                track.getCode(),
                track.getFromStation() != null ? track.getFromStation().getId() : null,
                track.getToStation() != null ? track.getToStation().getId() : null,
                track.getLengthKm(),
                track.getTravelCost(),
                track.getTravelTimeMinutes(),
                track.getCapacity(),
                track.getStatus()
        );
    }

    public static TrainDto toTrainDto(Train train) {
        if (train == null) {
            return null;
        }

        return new TrainDto(
                train.getId(),
                train.getCode(),
                train.getName(),
                train.getTrainType(),
                train.getPriority(),
                train.getMaxSpeedKmh(),
                train.getStatus()
        );
    }

    public static NetworkMapResponse toMapResponse(Network network, List<Station> stations, List<Track> tracks) {
        return new NetworkMapResponse(
                toDto(network),
                stations.stream().map(NetworkMapper::toStationDto).toList(),
                tracks.stream().map(NetworkMapper::toTrackDto).toList()
        );
    }
}
