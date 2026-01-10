/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                    MINECRAFT SERVER REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 */
package world.haorenfu.domain.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Minecraft server operations.
 */
@Repository
public interface MinecraftServerRepository extends JpaRepository<MinecraftServer, UUID> {

    /**
     * Finds the primary server.
     */
    Optional<MinecraftServer> findByPrimaryTrue();

    /**
     * Finds all enabled servers.
     */
    List<MinecraftServer> findByEnabledTrueOrderByPrimaryDescNameAsc();

    /**
     * Finds servers by type.
     */
    List<MinecraftServer> findByTypeAndEnabledTrue(MinecraftServer.ServerType type);

    /**
     * Finds server by host and port.
     */
    Optional<MinecraftServer> findByHostAndPort(String host, int port);

    /**
     * Counts online servers.
     */
    @Query("SELECT COUNT(s) FROM MinecraftServer s WHERE s.lastOnline = true AND s.enabled = true")
    long countOnlineServers();
}
