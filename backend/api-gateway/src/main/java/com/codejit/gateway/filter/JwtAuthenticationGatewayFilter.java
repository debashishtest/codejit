package com.codejit.gateway.filter;

import com.codejit.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationGatewayFilter extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilter.Config> {

    private final JwtUtils jwtUtils;

    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/v1/public/login",
            "/api/v1/public/register",
            "/api/v1/assessments/join",
            "/api/v1/interviews/join",
            "/actuator"
    );

    public JwtAuthenticationGatewayFilter(
            @Value("${jwt.secret:" + JwtUtils.DEFAULT_SECRET + "}") String jwtSecret,
            @Value("${jwt.expiration:" + JwtUtils.DEFAULT_EXPIRATION_MS + "}") long jwtExpiration) {
        super(Config.class);
        this.jwtUtils = new JwtUtils(jwtSecret, jwtExpiration);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Allow open public endpoints and WebSocket upgrades
            if (isOpenEndpoint(path)) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
            }

            String username = jwtUtils.extractUsername(token);
            String role = jwtUtils.extractRole(token);
            Long userId = jwtUtils.extractUserId(token);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Email", username != null ? username : "")
                    .header("X-User-Role", role != null ? role : "")
                    .header("X-User-Id", userId != null ? String.valueOf(userId) : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private boolean isOpenEndpoint(String path) {
        return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith) || path.startsWith("/ws");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}

