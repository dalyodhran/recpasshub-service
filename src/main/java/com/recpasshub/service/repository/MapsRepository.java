package com.recpasshub.service.repository;

import com.recpasshub.service.entity.Maps;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapsRepository extends JpaRepository<Maps, Long> {
    Optional<Maps> findByMapGuid(String mapGuid);
}
