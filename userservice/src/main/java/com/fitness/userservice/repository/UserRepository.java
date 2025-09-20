package com.fitness.userservice.repository;

import com.fitness.userservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    public boolean existsByEmail(String email);
    public boolean existsByKeycloakId(String id);

    public User findByEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid Email") String email);
}
