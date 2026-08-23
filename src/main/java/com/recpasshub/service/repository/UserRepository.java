package com.recpasshub.service.repository;

import com.recpasshub.service.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Explicitly query by the ID provided by the JWT token
    Optional<User> findByAuthProviderId(String authProviderId);
}
