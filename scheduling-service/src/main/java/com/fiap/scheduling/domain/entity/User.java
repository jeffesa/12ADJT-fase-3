package com.fiap.scheduling.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade de domínio User (POJO puro).
 * Representa um usuário do sistema hospitalar.
 */
public class User {

    private UUID id;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(UUID id, String name, String email, String password, UserRole role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User create(String name, String email, String encodedPassword, UserRole role) {
        return new User(UUID.randomUUID(), name, email, encodedPassword, role, LocalDateTime.now());
    }

    // Validações de domínio

    public void validateEmail() {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }

    public void validateName() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
