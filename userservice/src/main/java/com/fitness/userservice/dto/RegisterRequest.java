package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid Email")
    private String email;
    private String keycloakId;
    @NotBlank(message = "Email is required")
    @Size(min = 6, message = "Password must have atleast 6 character")
    private String password;
    private String firstName;
    private String lastName;

}
