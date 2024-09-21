package com.starwars.gateway.config.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.gateway.common.enums.UserStatusCodeEnum;
import com.starwars.gateway.common.utils.ResultVoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
    return Mono.defer(() -> Mono.just(exchange.getResponse()))
        .flatMap(response -> {
          response.setStatusCode(HttpStatus.OK);
          response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
          DataBufferFactory dataBufferFactory = response.bufferFactory();
          String result = null;
          log.info("CustomAccessDeniedHandler 🫸:{}", denied.getMessage());
          try {
            result = objectMapper.writeValueAsString(ResultVoUtil.failed(UserStatusCodeEnum.PERMISSION_DENIED));
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
          DataBuffer buffer = dataBufferFactory.wrap(result.getBytes(
              Charset.defaultCharset()));
          return response.writeWith(Mono.just(buffer));
        });
  }

}
