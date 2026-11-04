package com.taskmanager.user.repository;

import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Save and find user by email case-insensitively")
    void testFindByEmailIgnoreCase() {
        UserEntity user = UserEntity.builder()
                .email("Sarah.Connor@Example.com")
                .passwordHash("$2a$12$hash")
                .fullName("Sarah Connor")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByEmailIgnoreCase("sarah.connor@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Sarah Connor");
        assertThat(found.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("existsByEmailIgnoreCase returns true when user exists and false otherwise")
    void testExistsByEmailIgnoreCase() {
        UserEntity user = UserEntity.builder()
                .email("kyle.reese@example.com")
                .passwordHash("$2a$12$hash")
                .fullName("Kyle Reese")
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmailIgnoreCase("KYLE.REESE@EXAMPLE.COM")).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("unknown@example.com")).isFalse();
    }

    @Test
    @DisplayName("findByIdAndStatus retrieves active user but returns empty for deactivated user")
    void testFindByIdAndStatus() {
        UserEntity activeUser = UserEntity.builder()
                .email("active@example.com")
                .passwordHash("$2a$12$hash")
                .fullName("Active User")
                .status(UserStatus.ACTIVE)
                .build();
        UserEntity savedActive = userRepository.save(activeUser);

        UserEntity deactivatedUser = UserEntity.builder()
                .email("deactivated@example.com")
                .passwordHash("$2a$12$hash")
                .fullName("Deactivated User")
                .status(UserStatus.DEACTIVATED)
                .build();
        UserEntity savedDeactivated = userRepository.save(deactivatedUser);

        assertThat(userRepository.findByIdAndStatus(savedActive.getId(), UserStatus.ACTIVE)).isPresent();
        assertThat(userRepository.findByIdAndStatus(savedDeactivated.getId(), UserStatus.ACTIVE)).isEmpty();
        assertThat(userRepository.findByIdAndStatus(savedDeactivated.getId(), UserStatus.DEACTIVATED)).isPresent();
    }
}

