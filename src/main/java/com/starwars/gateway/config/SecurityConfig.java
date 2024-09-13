package com.starwars.gateway.config;

import com.starwars.gateway.config.authentication.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.LinkedList;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private CustomFormLogin customFormLogin;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private CustomSecurityContextRepository customSecurityContextRepository;

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        // 创建自定义 AuthenticationWebFilter 并设置 JsonServerAuthenticationConverter
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(new JsonServerAuthenticationConverter());

        // 自定义登录成功、失败处理器、登录接口、登录成功后 SecurityContext存储方式
        authenticationWebFilter.setAuthenticationSuccessHandler(customFormLogin.successHandler());
        authenticationWebFilter.setAuthenticationFailureHandler(customFormLogin.failureHandler());
        authenticationWebFilter.setSecurityContextRepository(customSecurityContextRepository);
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, new String[] { "/login" }));

        http
                .authenticationManager(reactiveAuthenticationManager())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION) // 替换默认的表单登录
                .securityContextRepository(customSecurityContextRepository)
                .authorizeExchange(
                        (exchanges) -> exchanges
                                .pathMatchers("/api/register").permitAll()
                                .pathMatchers("/api/*").authenticated())
                .exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    /**
     * 密码 加密方式
     * 
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 注册用户信息验证管理器，可按需求添加多个按顺序执行
     */
    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        LinkedList<ReactiveAuthenticationManager> managers = new LinkedList<>();
        UserDetailsRepositoryReactiveAuthenticationManager UserDetailsServiceAuthenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
                customUserDetailsService);
        /**
         * 添加密码加密方式
         */
        UserDetailsServiceAuthenticationManager.setPasswordEncoder(passwordEncoder()); // 设置 PasswordEncoder

        managers.add(UserDetailsServiceAuthenticationManager);
        // managers.add(tokenAuthenticationManager);
        return new DelegatingReactiveAuthenticationManager(managers);
    }
}
