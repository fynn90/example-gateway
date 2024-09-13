package com.starwars.gateway.config.authentication;

import com.starwars.gateway.model.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        log.info("查询用户：{}", username);
        WebClient webClient = WebClient.create("http://localhost:9111"); // 调用用户服务获取用户信息
        // 添加查询参数
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/user")
                        .queryParam("name", username) // 添加查询参数
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(userDto -> {
                    log.info("用户信息：{}", userDto.toString());
                    return User.builder()
                            .username(userDto.getUsername())
                            .password(userDto.getPassword()) // 已加密的密码，不需要再加密
                            .authorities(userDto.getAuthorities()) // 将权限设置
                            .build();
                })
                .onErrorResume(e -> {
                    log.error("查询用户失败: {}", e.getMessage());
                    return Mono.empty(); // Handle error case
                });
    }
}
