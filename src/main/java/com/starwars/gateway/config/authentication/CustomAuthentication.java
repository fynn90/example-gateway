package com.starwars.gateway.config.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomAuthentication implements Authentication {

    private final String username;

    private final String password;

    private List<CustomGrantedAuthority> authorities;

    private boolean isAuthenticated;

    public CustomAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<CustomGrantedAuthority> getAuthorities() {
        return this.authorities; // 可以根据需求返回用户权限
    }

    public void setAuthorities(@NonNull List<CustomGrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated; // 只要 Redis 校验成功，认为用户是已认证的
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}
