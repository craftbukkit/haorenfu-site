/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    AUTHENTICATED USER PROVIDER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Provides access to the currently authenticated user.
 * This is a UI-scoped bean that can be injected into Vaadin views.
 */
package world.haorenfu.core.security;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;
import world.haorenfu.domain.user.User;

import java.util.Optional;

/**
 * Provider for accessing the currently authenticated user.
 * Designed to be injected into Vaadin views.
 */
@Component
@UIScope
public class AuthenticatedUserProvider {

    private final AuthenticationService authenticationService;

    public AuthenticatedUserProvider(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return Optional containing the user if authenticated, empty otherwise
     */
    public Optional<User> get() {
        return authenticationService.getAuthenticatedUser();
    }

    /**
     * Checks if a user is currently authenticated.
     */
    public boolean isAuthenticated() {
        return authenticationService.isAuthenticated();
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        authenticationService.logout();
    }
}
