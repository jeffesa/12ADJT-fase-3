package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.User;
import com.fiap.scheduling.domain.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserJpaGateway.class)
class UserJpaGatewayTest {

    @Autowired
    private UserJpaGateway gateway;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private User buildUser(UserRole role) {
        return new User(
                UUID.randomUUID(),
                role == UserRole.ROLE_DOCTOR ? "Doutor" : "Paciente",
                role.name().toLowerCase() + "+" + UUID.randomUUID() + "@mail.com",
                "hash",
                role,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve criar usuário")
    void shouldCreate() {
        User user = buildUser(UserRole.ROLE_DOCTOR);
        User saved = gateway.create(user);
        assertNotNull(saved);
        assertEquals(user.getEmail(), saved.getEmail());
        assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void shouldFindById() {
        User user = gateway.create(buildUser(UserRole.ROLE_PATIENT));

        Optional<User> result = gateway.findById(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio para ID inexistente")
    void shouldReturnEmptyForNonExistentId() {
        assertTrue(gateway.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por email")
    void shouldFindByEmail() {
        User user = gateway.create(buildUser(UserRole.ROLE_NURSE));

        Optional<User> result = gateway.findByEmail(user.getEmail());

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    @DisplayName("Deve buscar por role")
    void shouldFindByRole() {
        gateway.create(buildUser(UserRole.ROLE_DOCTOR));
        gateway.create(buildUser(UserRole.ROLE_DOCTOR));
        gateway.create(buildUser(UserRole.ROLE_PATIENT));

        List<User> result = gateway.findByRole(UserRole.ROLE_DOCTOR);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve mapear domínio para JPA e voltar")
    void shouldMapDomainToJpaAndBack() {
        User user = buildUser(UserRole.ROLE_PATIENT);

        UserJpaEntity entity = UserJpaEntity.fromDomain(user);
        User restored = entity.toDomain();

        assertEquals(user.getId(), restored.getId());
        assertEquals(user.getName(), restored.getName());
        assertEquals(user.getEmail(), restored.getEmail());
        assertEquals(user.getRole(), restored.getRole());
    }
}
