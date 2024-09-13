package com.starwars.gateway.model;

import com.starwars.gateway.config.authentication.CustomGrantedAuthority;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
@Data
public class UserDto {
    private @NonNull String username;
    private @NonNull String password;
    private @NonNull List<CustomGrantedAuthority> authorities; // Assuming authorities are returned as a list of strings
}
