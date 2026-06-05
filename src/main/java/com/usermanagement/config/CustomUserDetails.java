package com.usermanagement.config;

import com.usermanagement.modelentity.BaseUser;
import com.usermanagement.modelentity.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security {@link UserDetails} adapter for any {@link BaseUser}.
 *
 * <p>Exposed extras (beyond the standard {@code UserDetails} API):
 * <ul>
 *   <li>{@link #getId()} — the database user ID, usable in controllers via
 *       {@code ((CustomUserDetails) authentication.getPrincipal()).getId()}</li>
 * </ul>
 *
 * Override by providing your own {@code UserDetailsService} bean.
 */
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Set<Role> roles;

    public CustomUserDetails(BaseUser user) {
        this.id       = user.getId();
        this.email    = user.getEmail();
        this.password = user.getPassword();
        this.roles    = user.getRoles();
    }

    /** Returns the database user ID. Use this instead of parsing the username. */
    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.getAuthority()))
                .collect(Collectors.toList());
    }

    @Override public String getPassword()               { return password; }
    @Override public String getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return true; }
}
