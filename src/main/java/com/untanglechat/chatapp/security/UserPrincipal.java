package com.untanglechat.chatapp.security;

import java.util.Collection;
import java.util.stream.Collectors;

import com.untanglechat.chatapp.models.Profile;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class UserPrincipal implements UserDetails{

    private final Profile profile;

    public UserPrincipal(final Profile profile) {
        this.profile = profile;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return profile.getRoles().stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
    }

    public String getPublicRSAKey() {
        return profile.getPublicRSAKey();
    }

    @Override
    public String getPassword() {
        return profile.getPassword();
    }

    @Override
    public String getUsername() {
        return profile.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return profile.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return profile.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return profile.isActive();
    }

    @Override
    public boolean isEnabled() {
        return profile.isActive();
    }
    
}
