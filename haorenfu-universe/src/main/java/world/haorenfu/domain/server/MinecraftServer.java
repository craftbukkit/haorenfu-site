/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        MINECRAFT SERVER ENTITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Server configuration entity for multi-server support.
 */
package world.haorenfu.domain.server;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Server configuration and status entity.
 */
@Entity
@Table(name = "mc_servers")
public class MinecraftServer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port = 25565;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServerType type = ServerType.JAVA;

    @Column(nullable = false)
    private boolean primary = false;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    private Instant lastChecked;

    private boolean lastOnline;

    private int lastOnlinePlayers;

    private int maxPlayers;

    @Column(length = 200)
    private String version;

    @Column(length = 500)
    private String motd;

    private long lastLatencyMs;

    // Constructors
    public MinecraftServer() {}

    public MinecraftServer(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    // Business methods

    public void updateStatus(ServerStatus status) {
        this.lastChecked = Instant.now();
        this.lastOnline = status.online();
        if (status.online()) {
            this.lastOnlinePlayers = status.onlinePlayers();
            this.maxPlayers = status.maxPlayers();
            this.version = status.version();
            this.motd = status.motd();
        }
    }

    public boolean isOnline() {
        return lastOnline && lastChecked != null &&
               Instant.now().minusSeconds(120).isBefore(lastChecked);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public ServerType getType() { return type; }
    public void setType(ServerType type) { this.type = type; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getLastChecked() { return lastChecked; }
    public boolean isLastOnline() { return lastOnline; }
    public int getLastOnlinePlayers() { return lastOnlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getVersion() { return version; }
    public String getMotd() { return motd; }
    public long getLastLatencyMs() { return lastLatencyMs; }

    /**
     * Server types supported.
     */
    public enum ServerType {
        JAVA("Java 版"),
        BEDROCK("基岩版"),
        MODDED("模组服"),
        PROXY("代理服");

        private final String displayName;

        ServerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
