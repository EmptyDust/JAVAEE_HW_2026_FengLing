package com.student.gateway.filter;

import com.student.common.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/register",
            "/auth/captcha",
            "/file/download",
            "/file/stream"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        for (String whitePath : WHITE_LIST) {
            if (path.contains(whitePath)) {
                return chain.filter(exchange);
            }
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (JwtUtil.isTokenExpired(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Long userId = JwtUtil.getUserId(token);
            String username = JwtUtil.getUsername(token);
            String userType = JwtUtil.getUserType(token);
            Long studentId = JwtUtil.getStudentId(token);

            ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                    .header("userId", String.valueOf(userId))
                    .header("username", username);

            if (userType != null) {
                builder.header("userType", userType);
            }
            if (studentId != null) {
                builder.header("studentId", String.valueOf(studentId));
            }

            ServerHttpRequest request = builder.build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
