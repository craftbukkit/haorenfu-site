/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                          USER SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Business logic layer for user operations.
 * Coordinates between repository, security, and other services.
 */
package world.haorenfu.domain.user;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.haorenfu.core.algorithm.BloomFilter;
import world.haorenfu.core.algorithm.EntropyAnalyzer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Service for user management operations.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Bloom filter for fast username existence checking
    private final BloomFilter<String> usernameFilter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        // Initialize Bloom filter with expected 10000 users, 1% false positive rate
        this.usernameFilter = new BloomFilter<>(10000, 0.01);

        // Populate filter with existing usernames
        userRepository.findAll().forEach(user ->
            usernameFilter.add(user.getUsername().toLowerCase())
        );
    }

    /**
     * Registers a new user.
     *
     * @param request Registration request
     * @return Created user
     * @throws UserRegistrationException if validation fails
     */
    public User register(RegistrationRequest request) {
        // Validate username availability (fast check with Bloom filter)
        if (usernameFilter.mightContain(request.username().toLowerCase())) {
            // Bloom filter says might exist, do actual check
            if (userRepository.existsByUsernameIgnoreCase(request.username())) {
                throw new UserRegistrationException("用户名已被使用");
            }
        }

        // Validate email availability
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new UserRegistrationException("邮箱已被注册");
        }

        // Validate password strength
        EntropyAnalyzer.PasswordStrength strength =
            EntropyAnalyzer.analyzePassword(request.password());

        if (strength.level().getScore() < EntropyAnalyzer.StrengthLevel.FAIR.getScore()) {
            throw new UserRegistrationException("密码强度不足: " + strength.feedback());
        }

        // Create user
        User user = new User(
            request.username(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        // Set optional fields
        if (request.minecraftId() != null && !request.minecraftId().isBlank()) {
            if (userRepository.existsByMinecraftIdIgnoreCase(request.minecraftId())) {
                throw new UserRegistrationException("该Minecraft ID已被绑定");
            }
            user.setMinecraftId(request.minecraftId());
        }

        // Save user
        user = userRepository.save(user);

        // Update Bloom filter
        usernameFilter.add(user.getUsername().toLowerCase());

        return user;
    }

    /**
     * Finds a user by ID.
     */
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by username.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Finds a user by email.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Updates user profile.
     */
    @CacheEvict(value = "users", key = "#userId")
    public User updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        if (request.signature() != null) {
            user.setSignature(request.signature());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }

        return userRepository.save(user);
    }

    /**
     * Changes user password.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("原密码错误");
        }

        EntropyAnalyzer.PasswordStrength strength =
            EntropyAnalyzer.analyzePassword(newPassword);

        if (strength.level().getScore() < EntropyAnalyzer.StrengthLevel.FAIR.getScore()) {
            throw new InvalidPasswordException("新密码强度不足: " + strength.feedback());
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Binds Minecraft ID to user.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void bindMinecraftId(UUID userId, String minecraftId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        if (userRepository.existsByMinecraftIdIgnoreCase(minecraftId)) {
            throw new MinecraftBindingException("该Minecraft ID已被其他用户绑定");
        }

        user.setMinecraftId(minecraftId);
        userRepository.save(user);
    }

    /**
     * Adds user to whitelist.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void addToWhitelist(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.setWhitelisted(true);
        userRepository.save(user);
    }

    /**
     * Removes user from whitelist.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void removeFromWhitelist(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.setWhitelisted(false);
        userRepository.save(user);
    }

    /**
     * Bans a user.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void banUser(UUID userId, String reason, Duration duration) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        Instant bannedUntil = duration != null ? Instant.now().plus(duration) : null;
        user.ban(reason, bannedUntil);
        userRepository.save(user);
    }

    /**
     * Unbans a user.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void unbanUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.unban();
        userRepository.save(user);
    }

    /**
     * Gets top users by reputation.
     */
    @Transactional(readOnly = true)
    public List<User> getTopUsersByReputation() {
        return userRepository.findTop10ByOrderByReputationDesc();
    }

    /**
     * Searches users.
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable);
    }

    /**
     * Gets all whitelisted users.
     */
    @Transactional(readOnly = true)
    public List<User> getWhitelistedUsers() {
        return userRepository.findByWhitelistedTrue();
    }

    /**
     * Gets user statistics.
     */
    @Transactional(readOnly = true)
    public UserStatistics getStatistics() {
        long totalUsers = userRepository.count();
        long newUsersToday = userRepository.countNewUsersSince(
            Instant.now().minus(Duration.ofDays(1))
        );
        long activeUsersToday = userRepository.countActiveUsersSince(
            Instant.now().minus(Duration.ofDays(1))
        );
        long whitelistedCount = userRepository.findByWhitelistedTrue().size();

        return new UserStatistics(totalUsers, newUsersToday, activeUsersToday, whitelistedCount);
    }

    /**
     * Verifies user email.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void verifyEmail(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Adds reputation to user.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void addReputation(UUID userId, int points) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.addReputation(points);
        userRepository.save(user);
    }

    /**
     * Records user login.
     */
    @CacheEvict(value = "users", key = "#userId")
    public void recordLogin(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));

        user.recordLogin();
        userRepository.save(user);
    }

    // Record classes for requests and responses

    public record RegistrationRequest(
        String username,
        String email,
        String password,
        String minecraftId
    ) {}

    public record ProfileUpdateRequest(
        String signature,
        String bio,
        String avatarUrl
    ) {}

    public record UserStatistics(
        long totalUsers,
        long newUsersToday,
        long activeUsersToday,
        long whitelistedUsers
    ) {}

    // Exception classes

    public static class UserRegistrationException extends RuntimeException {
        public UserRegistrationException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    public static class MinecraftBindingException extends RuntimeException {
        public MinecraftBindingException(String message) {
            super(message);
        }
    }
}
