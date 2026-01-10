/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                       AUTHENTICATED USER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Spring Security UserDetails implementation wrapping our User entity.
 */
package world.haorenfu.core.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import world.haorenfu.domain.user.Permission;
import world.haorenfu.domain.user.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UserDetails implementation that wraps our domain User.
 */
public class AuthenticatedUser implements UserDetails {

    private final UUID userId;
    private final String username;
    private final String passwordHash;
    private final boolean enabled;
    private final Set<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.enabled = !user.isBanned() && user.isEmailVerified();

        // Build authorities from role and permissions
        Set<GrantedAuthority> auths = new HashSet<>();

        // Add role
        auths.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Add permissions
        user.getPermissions().stream()
            .map(Permission::getCode)
            .map(SimpleGrantedAuthority::new)
            .forEach(auths::add);

        this.authorities = Collections.unmodifiableSet(auths);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Checks if user has a specific authority.
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals(authority));
    }

    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(String role) {
        return hasAuthority("ROLE_" + role);
    }
}
