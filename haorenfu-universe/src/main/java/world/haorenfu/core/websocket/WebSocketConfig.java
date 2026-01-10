/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                       WEBSOCKET CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Real-time communication setup for live notifications, chat, and updates.
 * Uses Spring WebSocket with STOMP protocol.
 */
package world.haorenfu.core.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket message broker configuration.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for subscriptions
        // Topics:
        //   /topic/server-status - Server status updates
        //   /topic/notifications - Global notifications
        //   /topic/chat - Public chat messages
        //   /topic/forum - Forum activity updates
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS(); // Fallback for browsers without WebSocket support
    }
}
