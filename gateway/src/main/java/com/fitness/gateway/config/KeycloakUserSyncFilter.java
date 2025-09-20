package com.fitness.gateway.config;

import com.fitness.gateway.User.UserService;
import com.fitness.gateway.dto.RegisterRequest;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        String userId = exchange.getRequest().getHeaders().getFirst("X-USER-ID");
        RegisterRequest request = getUserDetails(token);

        if(userId == null) userId = request.getKeycloakId();

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        log.info("User exist: {}", exist);
                        if (!exist) {
                            if (request != null){
                                return userService.registerUser(request).then(Mono.empty());
                            }else{
                                return Mono.empty();
                            }
                        } else {
                            log.info("User already exist... Skipping sync");
                            return Mono.empty();
                        }
                    }).then(Mono.defer(() ->{
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-USER-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }
        return chain.filter(exchange);
    }

    private RegisterRequest getUserDetails(String token) {

        try{
            String tokenWithoutBearer = token.replace("Bearer ", "");
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setKeycloakId(claims.getStringClaim("sub"));
            registerRequest.setFirstName(claims.getStringClaim("given_name"));
            registerRequest.setLastName(claims.getStringClaim("family_name"));
            registerRequest.setPassword("dummy@123");

            return registerRequest;

        }catch (Exception err){
            return null;
        }

    }
}
