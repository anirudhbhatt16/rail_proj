-- Human-readable seed reference copy for the demo railway network.
-- The authoritative migration remains backend-java/src/main/resources/db/migration/R__seed_demo_data.sql.

INSERT INTO networks (id, name, code, description, created_at, updated_at)
VALUES
    (1, 'North Corridor', 'NET-001', 'Primary demonstration network', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO stations (id, network_id, code, name, latitude, longitude, created_at, updated_at)
VALUES
    (1, 1, 'ST-001', 'Aster', 40.712800, -74.006000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'ST-002', 'Baker', 40.724000, -73.995000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 'ST-003', 'Cedar', 40.736000, -73.990000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 1, 'ST-004', 'Dover', 40.748000, -73.985000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 1, 'ST-005', 'Eden', 40.760000, -73.980000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tracks (id, network_id, from_station_id, to_station_id, code, length_km, travel_cost, travel_time_minutes, capacity, status, created_at, updated_at)
VALUES
    (1, 1, 1, 2, 'TR-101', 12.000, 12.000, 18.000, 1, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 2, 3, 'TR-102', 14.000, 14.000, 22.000, 1, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 3, 4, 'TR-103', 8.000, 8.000, 12.000, 1, 'BLOCKED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 1, 1, 4, 'TR-104', 20.000, 20.000, 30.000, 1, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 1, 4, 5, 'TR-105', 10.000, 10.000, 15.000, 1, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (6, 1, 2, 5, 'TR-106', 16.000, 16.000, 24.000, 1, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO trains (id, network_id, code, name, train_type, priority, max_speed_kmh, status, created_at, updated_at)
VALUES
    (1, 1, 'TRN-101', 'Express Rover', 'PASSENGER', 90, 140.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'TRN-202', 'Freight Mule', 'FREIGHT', 60, 80.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 1, 'TRN-303', 'Regional Local', 'REGIONAL', 35, 110.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO routes (id, network_id, code, name, description, created_at, updated_at)
VALUES
    (1, 1, 'ROUTE-01', 'Cedar Shuttle', 'Primary north corridor route', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 1, 'ROUTE-02', 'Eden Express', 'Alternative route around blocked track', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO route_segments (id, route_id, sequence_number, track_id, from_station_id, to_station_id, created_at)
VALUES
    (1, 1, 1, 1, 1, 2, CURRENT_TIMESTAMP),
    (2, 1, 2, 2, 2, 3, CURRENT_TIMESTAMP),
    (3, 2, 1, 1, 1, 2, CURRENT_TIMESTAMP),
    (4, 2, 2, 6, 2, 5, CURRENT_TIMESTAMP);
