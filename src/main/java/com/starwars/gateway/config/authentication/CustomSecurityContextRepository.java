package com.starwars.gateway.config.authentication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class CustomSecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    public ReactiveRedisTemplate<String, String> redisTemplate;

    private final static String SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT:";

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName();  // 获取用户 ID
            return exchange.getSession().flatMap(webSession -> {
                String sessionId = webSession.getId();
                // 将用户 ID 存储到 Redis，使用 sessionId 作为键
                exchange.getResponse().addCookie(
                        ResponseCookie.from("StarWarsID", sessionId).
                                secure(true)
                                .httpOnly(true)
                                .path("/")
                                .sameSite("Strict")
                                .maxAge(Duration.ofDays(30))
                                .build()
                );
                log.info("保存用户 ID 到 Redis，sessionId-useId: {}-{}", sessionId,userId);
                return redisTemplate.opsForValue().set(SECURITY_CONTEXT_KEY + sessionId, userId).then();
            });
        }
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        HttpCookie userCookie = exchange.getRequest().getCookies().getFirst("StarWarsID");
        if (userCookie != null) {
            String userId = userCookie.getValue();
            return redisTemplate.opsForValue().get(SECURITY_CONTEXT_KEY + userId).map(useName ->{
                log.info("从 Redis 获取用户 ID，sessionId-useId: {}-{}", userId, useName);
                if (useName != null) {
                    SecurityContextImpl securityContext = new SecurityContextImpl();
                    securityContext.setAuthentication(new CustomAuthentication(useName));
                    return securityContext;
                } else {
                    return null;
                }
        });
        } else {
            return Mono.empty();
        }
    }
}
