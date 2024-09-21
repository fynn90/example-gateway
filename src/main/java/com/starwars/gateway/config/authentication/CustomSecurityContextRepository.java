package com.starwars.gateway.config.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class CustomSecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    public ReactiveRedisTemplate<String, CustomAuthentication> redisTemplate;

    private final static String SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT:";

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        Authentication authentication = context.getAuthentication();
        log.info("保存用户认证信息到 SecurityContext {}", authentication);
        if (authentication != null && authentication.isAuthenticated()) {
            String userId = authentication.getName(); // 获取用户 ID
            return exchange.getSession().flatMap(webSession -> {
                String sessionId = webSession.getId();
                // 将用户 ID 存储到 Redis，使用 sessionId 作为键
                exchange.getResponse().addCookie(
                        ResponseCookie.from("StarWarsID", sessionId).secure(true)
                                .httpOnly(true)
                                .path("/")
                                .sameSite("Strict")
                                .maxAge(Duration.ofDays(30))
                                .build());
                User user = (User) authentication.getPrincipal();
                CustomAuthentication authentication1 = new CustomAuthentication(user.getUsername(), user.getPassword());
                authentication1.setAuthorities((List<CustomGrantedAuthority>) authentication.getAuthorities());
                authentication1.setAuthenticated(true);
                log.info("保存用户 ID 到 Redis，sessionId-useId-value: {}-{}", sessionId, userId);
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    log.info("Authentication object to be stored: {}",
                            objectMapper.writeValueAsString(authentication1));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                return redisTemplate.opsForValue().set(SECURITY_CONTEXT_KEY + sessionId, authentication1).then();
            });
        }
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        HttpCookie userCookie = exchange.getRequest().getCookies().getFirst("StarWarsID");
        if (userCookie != null) {
            String sessionId = userCookie.getValue();
            return redisTemplate.opsForValue().get(SECURITY_CONTEXT_KEY + sessionId)
                    .map(authentication -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            log.info("从 Redis 加载 authentication: {}", objectMapper.writeValueAsString(authentication));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        SecurityContextImpl securityContext = new SecurityContextImpl();
                        securityContext.setAuthentication(authentication);
                        return securityContext;
                    });
        } else {
            return Mono.empty();
        }
    }
}
