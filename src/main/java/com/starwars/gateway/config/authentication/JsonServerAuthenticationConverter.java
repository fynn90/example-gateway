package com.starwars.gateway.config.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 从 请求的 body 获取用户信息；
 */
@Slf4j
public class JsonServerAuthenticationConverter implements ServerAuthenticationConverter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return exchange.getRequest().getBody()
                .map(dataBuffer -> {
                    // 使用 DataBufferUtils 来读取 DataBuffer 的内容
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer); // 释放 DataBuffer
                    return new String(bytes, StandardCharsets.UTF_8); // 将字节转换为字符串
                })
                .collectList()
                .flatMap(bodyStrings -> {
                    String body = String.join("", bodyStrings); // 将请求体拼接为一个字符串
                    System.out.println(body);
                    try {
                        // 解析请求体 JSON
                        // 将请求体字符串解析为 Map，提取用户名和密码
                        Map<String, String> credentials = objectMapper.readValue(body,
                                new TypeReference<Map<String, String>>() {
                                });
                        String username = credentials.get("username");
                        String password = credentials.get("password");
                        log.info("Username: " + username);
                        log.info("Password: " + password);
                        if (username != null && password != null) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(username, password));
                        } else {
                            return Mono.empty();
                        }
                    } catch (Exception e) {
                        return Mono.empty(); // 返回空值表示转换失败
                    }
                });
    }

    // 内部类用于映射 JSON 请求体
    @Setter
    @Getter
    static class LoginRequest {
        private String username;
        private String password;
    }
}
