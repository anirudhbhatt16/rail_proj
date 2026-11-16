package com.railops.route;

import com.railops.station.Station;
import com.railops.track.Track;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "route_segments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_route_segments_route_sequence", columnNames = {"route_id", "sequence_number"})
        },
        indexes = {
                @Index(name = "idx_route_segments_route", columnList = "route_id"),
                @Index(name = "idx_route_segments_track", columnList = "track_id")
        })
public class RouteSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_station_id", nullable = false)
    private Station fromStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_station_id", nullable = false)
    private Station toStation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RouteSegment() {
    }

    public RouteSegment(Route route,
                       Integer sequenceNumber,
                       Track track,
                       Station fromStation,
                       Station toStation) {
        this.route = route;
        this.sequenceNumber = sequenceNumber;
        this.track = track;
        this.fromStation = fromStation;
        this.toStation = toStation;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public Track getTrack() {
        return track;
    }

    public Station getFromStation() {
        return fromStation;
    }

    public Station getToStation() {
        return toStation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
