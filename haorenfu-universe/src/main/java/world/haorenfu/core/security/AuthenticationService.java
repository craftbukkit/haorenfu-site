/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      AUTHENTICATION SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles user authentication and session management.
 */
package world.haorenfu.core.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Authentication service for managing user sessions.
 */
@Service
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
            .or(() -> userRepository.findByEmailIgnoreCase(username))
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return new AuthenticatedUser(user);
    }

    /**
     * Gets the currently authenticated user.
     */
    public Optional<User> getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        if (authentication == null || 
            authentication instanceof AnonymousAuthenticationToken ||
            !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authUser) {
            return userRepository.findById(authUser.getUserId());
        }

        return Optional.empty();
    }

    /**
     * Checks if a user is currently authenticated.
     */
    public boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication != null &&
               !(authentication instanceof AnonymousAuthenticationToken) &&
               authentication.isAuthenticated();
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        UI.getCurrent().getPage().setLocation("/");
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
            VaadinServletRequest.getCurrent().getHttpServletRequest(),
            null,
            null
        );
    }

    /**
     * Gets the current user's ID.
     */
    public Optional<UUID> getCurrentUserId() {
        return getAuthenticatedUser().map(User::getId);
    }
}
