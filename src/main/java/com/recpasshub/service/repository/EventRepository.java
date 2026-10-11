package com.recpasshub.service.repository;

import com.recpasshub.service.entity.Event;
import com.recpasshub.service.entity.EventState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByEventGuid(String eventId);

    /**
     * Finds events in the given state whose map start point lies within the bounding box.
     * Maps without valid coordinates never match. Callers apply the precise distance filter.
     */
    @EntityGraph(attributePaths = "map")
    List<Event> findByStateAndMap_LatitudeBetweenAndMap_LongitudeBetween(
        EventState state,
        double minLatitude,
        double maxLatitude,
        double minLongitude,
        double maxLongitude
    );
}
