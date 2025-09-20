package com.fitness.userservice.dto;

import com.fitness.userservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;

@Data
@Builder
public class UserResponse {
    private String id;

    private String keycloakId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    private UserRole role = UserRole.USER;
    private LocalTime createdAt;

    private LocalTime updatedAt;
}
