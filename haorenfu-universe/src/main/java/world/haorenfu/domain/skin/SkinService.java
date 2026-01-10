/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                           SKIN SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Business logic for skin management including validation,
 * image processing, and analytics.
 */
package world.haorenfu.domain.skin;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SkinService {

    private final SkinRepository skinRepository;

    // Skin image dimensions
    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;
    private static final int CAPE_WIDTH = 64;
    private static final int CAPE_HEIGHT = 32;

    // Max file sizes (in bytes)
    private static final int MAX_SKIN_SIZE = 64 * 1024; // 64KB
    private static final int MAX_CAPE_SIZE = 32 * 1024; // 32KB

    public SkinService(SkinRepository skinRepository) {
        this.skinRepository = skinRepository;
    }

    /**
     * Creates a new skin entry.
     */
    @CacheEvict(value = "skins", allEntries = true)
    public Skin createSkin(String name, UUID ownerId, String ownerUsername,
                           String skinData, Skin.SkinModel model, Skin.SkinCategory category) {
        // Validate skin data
        validateSkinData(skinData);

        Skin skin = new Skin(name, ownerId, ownerUsername, skinData);
        skin.setModel(model);
        skin.setCategory(category);

        return skinRepository.save(skin);
    }

    /**
     * Updates an existing skin.
     */
    @CacheEvict(value = "skins", allEntries = true)
    public Skin updateSkin(UUID skinId, UUID userId, String name, String description,
                           Skin.SkinVisibility visibility, Skin.SkinCategory category) {
        Skin skin = skinRepository.findById(skinId)
            .orElseThrow(() -> new IllegalArgumentException("Skin not found"));

        // Only owner can update
        if (!skin.getOwnerId().equals(userId)) {
            throw new SecurityException("Not authorized to update this skin");
        }

        skin.setName(name);
        skin.setDescription(description);
        skin.setVisibility(visibility);
        skin.setCategory(category);

        return skinRepository.save(skin);
    }

    /**
     * Deletes a skin.
     */
    @CacheEvict(value = "skins", allEntries = true)
    public void deleteSkin(UUID skinId, UUID userId) {
        Skin skin = skinRepository.findById(skinId)
            .orElseThrow(() -> new IllegalArgumentException("Skin not found"));

        if (!skin.getOwnerId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this skin");
        }

        skinRepository.delete(skin);
    }

    /**
     * Gets a skin by ID and increments view count.
     */
    @Transactional
    public Optional<Skin> getSkinAndRecordView(UUID skinId) {
        return skinRepository.findById(skinId).map(skin -> {
            skin.incrementViews();
            return skinRepository.save(skin);
        });
    }

    /**
     * Downloads a skin and increments download count.
     */
    @Transactional
    public Optional<String> downloadSkin(UUID skinId) {
        return skinRepository.findById(skinId).map(skin -> {
            skin.incrementDownloads();
            skinRepository.save(skin);
            return skin.getSkinData();
        });
    }

    /**
     * Likes a skin.
     */
    @Transactional
    public void likeSkin(UUID skinId) {
        skinRepository.findById(skinId).ifPresent(skin -> {
            skin.like();
            skinRepository.save(skin);
        });
    }

    /**
     * Unlikes a skin.
     */
    @Transactional
    public void unlikeSkin(UUID skinId) {
        skinRepository.findById(skinId).ifPresent(skin -> {
            skin.unlike();
            skinRepository.save(skin);
        });
    }

    /**
     * Gets public skins with pagination.
     */
    @Cacheable(value = "skins", key = "'public-' + #pageable.pageNumber")
    public Page<Skin> getPublicSkins(Pageable pageable) {
        return skinRepository.findByVisibilityOrderByCreatedAtDesc(
            Skin.SkinVisibility.PUBLIC, pageable);
    }

    /**
     * Gets skins by category.
     */
    public Page<Skin> getSkinsByCategory(Skin.SkinCategory category, Pageable pageable) {
        return skinRepository.findByCategoryAndVisibilityOrderByCreatedAtDesc(
            category, Skin.SkinVisibility.PUBLIC, pageable);
    }

    /**
     * Gets user's skins.
     */
    public Page<Skin> getUserSkins(UUID userId, Pageable pageable) {
        return skinRepository.findByOwnerIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Gets most downloaded skins.
     */
    @Cacheable(value = "skins", key = "'top-downloads'")
    public List<Skin> getMostDownloadedSkins(int limit) {
        return skinRepository.findMostDownloaded(PageRequest.of(0, limit));
    }

    /**
     * Gets most liked skins.
     */
    @Cacheable(value = "skins", key = "'top-likes'")
    public List<Skin> getMostLikedSkins(int limit) {
        return skinRepository.findMostLiked(PageRequest.of(0, limit));
    }

    /**
     * Gets featured skins.
     */
    @Cacheable(value = "skins", key = "'featured'")
    public List<Skin> getFeaturedSkins() {
        return skinRepository.findFeatured();
    }

    /**
     * Searches skins by name or description.
     */
    public Page<Skin> searchSkins(String query, Pageable pageable) {
        return skinRepository.search(query.trim(), pageable);
    }

    /**
     * Gets skins by tag.
     */
    public Page<Skin> getSkinsByTag(String tag, Pageable pageable) {
        return skinRepository.findByTag(tag.toLowerCase().trim(), pageable);
    }

    /**
     * Gets user skin statistics.
     */
    public UserSkinStats getUserStats(UUID userId) {
        long skinCount = skinRepository.countByOwnerId(userId);
        Long totalDownloads = skinRepository.totalDownloadsByOwner(userId);

        return new UserSkinStats(
            skinCount,
            totalDownloads != null ? totalDownloads : 0
        );
    }

    /**
     * Sets a skin as featured (admin only).
     */
    @CacheEvict(value = "skins", key = "'featured'")
    public void setFeatured(UUID skinId, boolean featured) {
        skinRepository.findById(skinId).ifPresent(skin -> {
            skin.setFeatured(featured);
            skinRepository.save(skin);
        });
    }

    /**
     * Validates skin image data.
     */
    private void validateSkinData(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            throw new IllegalArgumentException("Skin data cannot be empty");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            if (decoded.length > MAX_SKIN_SIZE) {
                throw new IllegalArgumentException("Skin file too large (max 64KB)");
            }

            // Check PNG signature
            if (decoded.length < 8 ||
                decoded[0] != (byte) 0x89 ||
                decoded[1] != 'P' ||
                decoded[2] != 'N' ||
                decoded[3] != 'G') {
                throw new IllegalArgumentException("Invalid skin format (must be PNG)");
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Skin")) throw e;
            throw new IllegalArgumentException("Invalid base64 encoding");
        }
    }

    /**
     * Validates cape image data.
     */
    private void validateCapeData(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) return;

        try {
            byte[] decoded = Base64.getDecoder().decode(base64Data);

            if (decoded.length > MAX_CAPE_SIZE) {
                throw new IllegalArgumentException("Cape file too large (max 32KB)");
            }

            // Check PNG signature
            if (decoded.length < 8 ||
                decoded[0] != (byte) 0x89 ||
                decoded[1] != 'P' ||
                decoded[2] != 'N' ||
                decoded[3] != 'G') {
                throw new IllegalArgumentException("Invalid cape format (must be PNG)");
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Cape")) throw e;
            throw new IllegalArgumentException("Invalid base64 encoding");
        }
    }

    /**
     * User skin statistics.
     */
    public record UserSkinStats(long skinCount, long totalDownloads) {}
}
