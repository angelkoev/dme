package com.akoev.dme.infrastructure.security;

import com.akoev.dme.domain.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User domainUser;

    public CustomUserDetails(User domainUser) {
        this.domainUser = domainUser;
    }

    public Long getId() {
        return domainUser.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return domainUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return domainUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return domainUser.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return domainUser.isEnabled();
    }
}
