package com.fitness.gateway.dto;

import lombok.Builder;
import lombok.Data;

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
    private LocalTime createdAt;
    private LocalTime updatedAt;
}
