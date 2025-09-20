package com.fitness.gateway.User;

import com.fitness.gateway.dto.RegisterRequest;
import com.fitness.gateway.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId){
        log.info("Calling user validation... : {}", userId);
            return userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(WebClientResponseException.class, err -> {
                        if(err.getStatusCode() == HttpStatus.NOT_FOUND) {
                            return Mono.error(new RuntimeException("User not found " + userId));
                        }
                        else if (err.getStatusCode() == HttpStatus.BAD_REQUEST) {
                            return Mono.error(new RuntimeException("Invalid Request " + userId));
                        }
                        return Mono.error(new RuntimeException("Unexpected Error" + userId));
                    });
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {
        log.info("Calling user Registration API for for email: {}", request.getEmail());
        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, err -> {
                    if(err.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new RuntimeException("BAD REQUEST " + err.getMessage()));
                    }
                    else if (err.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        return Mono.error(new RuntimeException("Internal Server error " + err.getMessage()));
                    }
                    return Mono.error(new RuntimeException("Unexpected Error" + err.getMessage()));
                });
    }
}
