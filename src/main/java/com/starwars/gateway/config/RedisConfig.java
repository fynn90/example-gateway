package com.starwars.gateway.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.starwars.gateway.config.authentication.CustomAuthentication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ReactiveRedisTemplate<String, CustomAuthentication> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory factory) {
    // 创建 ObjectMapper
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new ParameterNamesModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.activateDefaultTyping(
        objectMapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.WRAPPER_OBJECT);

    // 使用构造器将 ObjectMapper 传递给 Jackson2JsonRedisSerializer
    Jackson2JsonRedisSerializer<CustomAuthentication> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
        CustomAuthentication.class);

    // 配置 Redis 序列化上下文
    RedisSerializationContext<String, CustomAuthentication> context = RedisSerializationContext
        .<String, CustomAuthentication>newSerializationContext(new StringRedisSerializer())
        .value(serializer)
        .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}
