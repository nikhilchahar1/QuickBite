package com.quickbite.gateway.filter;

import com.quickbite.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {

        // This lambda runs for every request that uses this filter
        return (exchange, chain) -> {

            // Get the Authorization header
            String authHeader = exchange.getRequest()
                                        .getHeaders()
                                        .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("No JWT token in request to: {}", exchange.getRequest().getPath());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isTokenValid(token)) {
                log.warn("Invalid JWT token");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            log.info("Authenticated request from user: {} role: {}", email, role);

            // modifies the request
            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Id", userId.toString())
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                    )
                    .build();

            // Continue to the actual service
            return chain.filter(modifiedExchange);
        };
    }

    // Returns an error response when auth fails
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    // Config class required by AbstractGatewayFilterFactory
    public static class Config {}
}