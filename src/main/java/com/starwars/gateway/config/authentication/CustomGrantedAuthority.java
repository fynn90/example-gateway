package com.starwars.gateway.config.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomGrantedAuthority implements GrantedAuthority {
    private String authority;
}
