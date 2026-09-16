package com.recpasshub.service.repository;

import com.recpasshub.service.entity.Waiver;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaiverRepository extends JpaRepository<Waiver, Long> {
    Optional<Waiver> findByWaiverGuid(String waiverGuid);
}
