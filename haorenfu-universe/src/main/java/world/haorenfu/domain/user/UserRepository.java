/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         USER REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Data access layer for user entities.
 * Leverages Spring Data JPA's query derivation for clean, type-safe queries.
 */
package world.haorenfu.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by username (case-insensitive).
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Finds a user by email (case-insensitive).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Finds a user by Minecraft ID.
     */
    Optional<User> findByMinecraftIdIgnoreCase(String minecraftId);

    /**
     * Finds a user by Minecraft UUID.
     */
    Optional<User> findByMinecraftUuid(String uuid);

    /**
     * Checks if a username already exists.
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Checks if an email already exists.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Checks if a Minecraft ID already exists.
     */
    boolean existsByMinecraftIdIgnoreCase(String minecraftId);

    /**
     * Finds all whitelisted users.
     */
    List<User> findByWhitelistedTrue();

    /**
     * Finds all users with a specific role.
     */
    List<User> findByRole(Role role);

    /**
     * Finds users by role with pagination.
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Finds currently banned users.
     */
    @Query("SELECT u FROM User u WHERE u.banned = true AND (u.bannedUntil IS NULL OR u.bannedUntil > :now)")
    List<User> findCurrentlyBanned(@Param("now") Instant now);

    /**
     * Finds users whose ban has expired.
     */
    @Query("SELECT u FROM User u WHERE u.banned = true AND u.bannedUntil IS NOT NULL AND u.bannedUntil <= :now")
    List<User> findExpiredBans(@Param("now") Instant now);

    /**
     * Finds users active in the last N hours.
     */
    @Query("SELECT u FROM User u WHERE u.lastActiveAt >= :since")
    List<User> findActiveUsersSince(@Param("since") Instant since);

    /**
     * Counts active users in a time period.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActiveAt >= :since")
    long countActiveUsersSince(@Param("since") Instant since);

    /**
     * Finds top users by reputation.
     */
    List<User> findTop10ByOrderByReputationDesc();

    /**
     * Finds users by reputation range.
     */
    Page<User> findByReputationBetween(int min, int max, Pageable pageable);

    /**
     * Searches users by username or Minecraft ID.
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.minecraftId) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    /**
     * Gets user statistics.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countNewUsersSince(@Param("since") Instant since);

    /**
     * Finds users needing email verification (older than threshold).
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :threshold")
    List<User> findUnverifiedUsersOlderThan(@Param("threshold") Instant threshold);

    /**
     * Bulk update last active timestamp for online players.
     */
    @Query("UPDATE User u SET u.lastActiveAt = :now WHERE u.minecraftUuid IN :uuids")
    void updateLastActiveForPlayers(@Param("uuids") List<String> uuids, @Param("now") Instant now);
}
