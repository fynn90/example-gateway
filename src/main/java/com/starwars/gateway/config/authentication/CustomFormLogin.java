package com.starwars.gateway.config.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.gateway.common.enums.UserStatusCodeEnum;
import com.starwars.gateway.common.utils.ResultVoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Slf4j
@Service
public class CustomFormLogin implements Customizer<ServerHttpSecurity.FormLoginSpec> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void customize(ServerHttpSecurity.FormLoginSpec formLoginSpec) {
        formLoginSpec
                .authenticationSuccessHandler(successHandler())
                .authenticationFailureHandler(failureHandler());
    }

    public ServerAuthenticationSuccessHandler successHandler() {
        return (webFilterExchange, authentication) -> {
            // 登录成功地处理逻辑
            return Mono.defer(() -> Mono.just(webFilterExchange.getExchange().getResponse()))
                    .flatMap(response -> {
                        response.setStatusCode(org.springframework.http.HttpStatus.OK);
                        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                        DataBufferFactory dataBufferFactory = response.bufferFactory();
                        String id = response.getCookies().getFirst("StarWarsID").getValue();
                        log.info("登录成功: " + id);
                        // 使用 Jackson 序列化对象
                        String result;
                        try {
                            result = objectMapper.writeValueAsString(ResultVoUtil.success(id));
                        } catch (Exception e) {
                            result = "{\"code\": 500, \"msg\": \"Internal Server Error\"}";
                        }
                        DataBuffer buffer = dataBufferFactory.wrap(result.getBytes(
                                Charset.defaultCharset()));
                        return response.writeWith(Mono.just(buffer));
                    });
        };
    }

    public ServerAuthenticationFailureHandler failureHandler() {
        return (webFilterExchange, exception) -> {
            // 登录失败的处理逻辑
            log.info("登录失败: " + exception.getMessage());
            // 登录成功地处理逻辑
            return Mono.defer(() -> Mono.just(webFilterExchange.getExchange().getResponse()))
                    .flatMap(response -> {
                        response.setStatusCode(org.springframework.http.HttpStatus.OK);
                        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                        DataBufferFactory dataBufferFactory = response.bufferFactory();
                        // 使用 Jackson 序列化对象
                        String result;
                        try {
                            result = objectMapper
                                    .writeValueAsString(ResultVoUtil.failed(UserStatusCodeEnum.LOGIN_PASSWORD_ERROR));
                        } catch (Exception e) {
                            result = "{\"code\": 500, \"msg\": \"Internal Server Error\"}";
                        }
                        DataBuffer buffer = dataBufferFactory.wrap(result.getBytes(
                                Charset.defaultCharset()));
                        return response.writeWith(Mono.just(buffer));
                    });
        };
    }
}
