package com.fiap.scheduling.infra.persistence;

import com.fiap.scheduling.domain.entity.Appointment;
import com.fiap.scheduling.domain.entity.AppointmentStatus;
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
@Import({AppointmentJpaGateway.class, UserJpaGateway.class})
class AppointmentJpaGatewayTest {

    @Autowired
    private AppointmentJpaGateway gateway;

    @Autowired
    private UserJpaGateway userGateway;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID patientId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        userRepository.deleteAll();

        patientId = userGateway.create(buildUser(UserRole.ROLE_PATIENT)).getId();
        doctorId = userGateway.create(buildUser(UserRole.ROLE_DOCTOR)).getId();
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

    private Appointment buildAppointment() {
        LocalDateTime now = LocalDateTime.now();
        return new Appointment(
                UUID.randomUUID(),
                patientId,
                doctorId,
                now.plusDays(1),
                AppointmentStatus.SCHEDULED,
                "Consulta",
                now,
                now
        );
    }

    @Test
    @DisplayName("Deve criar consulta")
    void shouldCreate() {
        Appointment saved = gateway.create(buildAppointment());

        assertNotNull(saved);
        assertTrue(appointmentRepository.findById(saved.getId()).isPresent());
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void shouldFindById() {
        Appointment saved = gateway.create(buildAppointment());

        Optional<Appointment> result = gateway.findById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Deve retornar vazio para ID inexistente")
    void shouldReturnEmptyForNonExistentId() {
        assertTrue(gateway.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por patientId")
    void shouldFindByPatientId() {
        gateway.create(buildAppointment());
        gateway.create(buildAppointment());

        List<Appointment> result = gateway.findByPatientId(patientId);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Deve buscar por doctorId")
    void shouldFindByDoctorId() {
        gateway.create(buildAppointment());

        List<Appointment> result = gateway.findByDoctorId(doctorId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve buscar por intervalo de data")
    void shouldFindByDateRange() {
        gateway.create(buildAppointment());

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        List<Appointment> result = gateway.findByDateRange(start, end);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve buscar consultas futuras")
    void shouldFindUpcoming() {
        gateway.create(buildAppointment());

        List<Appointment> result = gateway.findUpcoming(LocalDateTime.now());

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Deve mapear domínio para JPA e voltar")
    void shouldMapDomainToJpaAndBack() {
        Appointment appointment = buildAppointment();
        UserJpaEntity patient = userRepository.findById(patientId).orElseThrow();
        UserJpaEntity doctor = userRepository.findById(doctorId).orElseThrow();

        AppointmentJpaEntity entity = AppointmentJpaEntity.fromDomain(appointment, patient, doctor);
        Appointment restored = entity.toDomain();

        assertEquals(appointment.getId(), restored.getId());
        assertEquals(appointment.getPatientId(), restored.getPatientId());
        assertEquals(appointment.getDoctorId(), restored.getDoctorId());
    }
}
