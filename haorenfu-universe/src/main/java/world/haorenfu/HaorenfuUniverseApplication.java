/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         HAORENFU UNIVERSE
 *                    Minecraft Community Platform
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This is the heart of our community - a place where players connect,
 * share experiences, and build lasting friendships through the world
 * of Minecraft.
 *
 * Architecture Philosophy:
 * - Clean separation of concerns
 * - Domain-driven design
 * - Mathematical elegance in algorithms
 * - Pure Java implementation (no JavaScript required)
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
package world.haorenfu;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for Haorenfu Universe.
 *
 * This application demonstrates how a full-featured web platform can be built
 * entirely in Java, leveraging Vaadin's component-based architecture for the
 * frontend and Spring Boot's robust ecosystem for the backend.
 *
 * Key Features:
 * - Real-time updates via WebSocket (Push)
 * - Progressive Web App support
 * - Asynchronous processing for better performance
 * - Scheduled tasks for background operations
 * - Intelligent caching strategies
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@Theme("haorenfu-universe")
@PWA(
    name = "好人服宇宙 - Minecraft Community",
    shortName = "好人服",
    description = "A vibrant Minecraft community platform",
    backgroundColor = "#1a1a2e",
    themeColor = "#16213e"
)
@Push(PushMode.AUTOMATIC)
public class HaorenfuUniverseApplication implements AppShellConfigurator {

    /**
     * Application entry point.
     *
     * The journey of a thousand blocks begins with a single click.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HaorenfuUniverseApplication.class, args);
    }
}
