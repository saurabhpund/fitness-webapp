package com.fitness.userservice.service;

import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.User;
import com.fitness.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository repository;

    public UserResponse register(RegisterRequest registerRequest){
        if(repository.existsByEmail(registerRequest.getEmail())){
            User existingUser = repository.findByEmail(registerRequest.getEmail());
            return UserResponse
                    .builder()
                    .id(existingUser.getId())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .firstName(existingUser.getFirstName())
                    .lastName(existingUser.getLastName())
                    .createdAt(existingUser.getCreatedAt())
                    .updatedAt(existingUser.getUpdatedAt()).build();
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setKeycloakId(registerRequest.getKeycloakId());
        user.setPassword(registerRequest.getPassword());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        User savedUser = repository.save(user);
        return  UserResponse
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }

    public UserResponse getUserProfile(String id){
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not Found"));

        return  UserResponse
                .builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }

    public Boolean existByUserId(String userId) {
        log.info("Call services to check user: {}", userId);
        return repository.existsById(userId);
    }

    public Boolean existByKeycloakId(String id) {
        return repository.existsByKeycloakId(id);
    }
}
