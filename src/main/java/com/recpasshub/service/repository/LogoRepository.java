package com.recpasshub.service.repository;

import com.recpasshub.service.entity.Logo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogoRepository extends JpaRepository<Logo, Long> {
    Optional<Logo> findByLogoGuid(String logoGuid);
}
