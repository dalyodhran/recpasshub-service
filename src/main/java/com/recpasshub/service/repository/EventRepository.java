package com.recpasshub.service.repository;

import com.recpasshub.service.entity.Event;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventGuid(String eventId);

    /**
     * Finds PUBLISHED events whose coordinates fall within a bounding box and within
     * {@code radiusKm} kilometers of ({@code latitude}, {@code longitude}), using the
     * Haversine formula for the precise distance filter.
     */
    @Query(
        value = """
        SELECT * FROM events e
        WHERE e.state = 'PUBLISHED'
          AND e.latitude IS NOT NULL
          AND e.longitude IS NOT NULL
          AND e.latitude BETWEEN :minLatitude AND :maxLatitude
          AND e.longitude BETWEEN :minLongitude AND :maxLongitude
          AND (
            6371 * acos(
              LEAST(1.0, GREATEST(-1.0,
                cos(radians(:latitude)) * cos(radians(e.latitude)) * cos(radians(e.longitude) - radians(:longitude))
                  + sin(radians(:latitude)) * sin(radians(e.latitude))
              ))
            )
          ) <= :radiusKm
        """,
        nativeQuery = true
    )
    List<Event> findNearbyPublishedEvents(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("minLatitude") double minLatitude,
        @Param("maxLatitude") double maxLatitude,
        @Param("minLongitude") double minLongitude,
        @Param("maxLongitude") double maxLongitude,
        @Param("radiusKm") double radiusKm
    );
}
