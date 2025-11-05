package org.userservice.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.userservice.user_service.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Custom query method to find a user by email
    Optional<UserEntity> findByEmail(String email);

    // Find users by name containing a substring (case-insensitive)
    List<UserEntity> findByUsernameContainingIgnoreCase(String username);

    // Find users created after a certain date
    List<UserEntity> findByCreatedAtAfter(java.time.LocalDateTime date);

}
