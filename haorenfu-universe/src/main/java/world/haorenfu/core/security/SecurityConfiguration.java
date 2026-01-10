/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        SECURITY CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Spring Security configuration for authentication and authorization.
 * Integrates with Vaadin's security model for seamless protection.
 */
package world.haorenfu.core.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import world.haorenfu.ui.view.LoginView;

/**
 * Security configuration using modern best practices.
 *
 * Features:
 * - Argon2 password hashing (winner of Password Hashing Competition)
 * - CSRF protection
 * - Session management
 * - Remember-me functionality
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends VaadinWebSecurity {

    /**
     * Password encoder using Argon2id.
     *
     * Argon2 won the Password Hashing Competition in 2015 and is considered
     * the most secure password hashing algorithm available.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Spring Security 6 compatible Argon2 encoder
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public resources
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                new AntPathRequestMatcher("/"),
                new AntPathRequestMatcher("/home"),
                new AntPathRequestMatcher("/rules"),
                new AntPathRequestMatcher("/join"),
                new AntPathRequestMatcher("/register"),
                new AntPathRequestMatcher("/forum"),
                new AntPathRequestMatcher("/forum/**"),
                new AntPathRequestMatcher("/server"),
                new AntPathRequestMatcher("/wiki"),
                new AntPathRequestMatcher("/wiki/**"),
                new AntPathRequestMatcher("/api/public/**"),
                new AntPathRequestMatcher("/images/**"),
                new AntPathRequestMatcher("/icons/**")
            ).permitAll()
        );

        super.configure(http);

        // Configure login view
        setLoginView(http, LoginView.class);
    }
}
