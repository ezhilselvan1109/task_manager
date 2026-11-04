package com.taskmanager.user.repository;

import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByIdAndStatus(UUID id, UserStatus status);
}

