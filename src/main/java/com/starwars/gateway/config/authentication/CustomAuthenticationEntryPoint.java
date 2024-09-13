package com.starwars.gateway.config.authentication;

import com.starwars.gateway.common.enums.UserStatusCodeEnum;
import com.starwars.gateway.common.utils.ResultVoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.security.core.AuthenticationException;
import java.nio.charset.Charset;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper(); // 添加 ObjectMapper 实例

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.defer(() -> Mono.just(exchange.getResponse()))
                .flatMap(response -> {
                    log.info("用户未登录或 token 已过期，请重新登录！");
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBufferFactory dataBufferFactory = response.bufferFactory();
                    // 使用 Jackson 序列化对象
                    String result;
                    try {
                        result = objectMapper.writeValueAsString(ResultVoUtil.failed(UserStatusCodeEnum.USER_UNAUTHORIZED));
                    } catch (Exception e) {
                        result = "{\"code\": 500, \"msg\": \"Internal Server Error\"}";
                    }
                    DataBuffer buffer = dataBufferFactory.wrap(result.getBytes(
                            Charset.defaultCharset()));
                    return response.writeWith(Mono.just(buffer));
                });
    }

}
