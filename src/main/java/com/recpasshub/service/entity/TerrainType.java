package com.recpasshub.service.entity;

import java.util.Set;

public enum TerrainType {
    POOL,
    LAKE,
    OCEAN,
    ROAD,
    GRAVEL,
    TRAIL,
    BOTH;

    public static Set<TerrainType> forSport(SportType sportType) {
        return switch (sportType) {
            case SWIMMING -> Set.of(POOL, LAKE, OCEAN);
            case CYCLING -> Set.of(ROAD, GRAVEL, BOTH);
            case RUNNING -> Set.of(ROAD, TRAIL, BOTH);
        };
    }

    public static boolean isValidFor(SportType sportType, TerrainType terrainType) {
        return terrainType == null || forSport(sportType).contains(terrainType);
    }
}
