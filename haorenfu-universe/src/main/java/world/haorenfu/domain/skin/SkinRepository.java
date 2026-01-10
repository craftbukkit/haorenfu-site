/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         SKIN REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 */
package world.haorenfu.domain.skin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkinRepository extends JpaRepository<Skin, UUID> {

    Page<Skin> findByVisibilityOrderByCreatedAtDesc(Skin.SkinVisibility visibility, Pageable pageable);

    Page<Skin> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    Page<Skin> findByCategoryAndVisibilityOrderByCreatedAtDesc(
        Skin.SkinCategory category, Skin.SkinVisibility visibility, Pageable pageable);

    @Query("SELECT s FROM Skin s WHERE s.visibility = 'PUBLIC' ORDER BY s.downloads DESC")
    List<Skin> findMostDownloaded(Pageable pageable);

    @Query("SELECT s FROM Skin s WHERE s.visibility = 'PUBLIC' ORDER BY s.likes DESC")
    List<Skin> findMostLiked(Pageable pageable);

    @Query("SELECT s FROM Skin s WHERE s.visibility = 'PUBLIC' AND s.featured = true ORDER BY s.createdAt DESC")
    List<Skin> findFeatured();

    @Query("SELECT s FROM Skin s WHERE s.visibility = 'PUBLIC' AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Skin> search(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Skin s JOIN s.tags t WHERE t = :tag AND s.visibility = 'PUBLIC'")
    Page<Skin> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Skin s WHERE s.ownerId = :ownerId")
    long countByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT SUM(s.downloads) FROM Skin s WHERE s.ownerId = :ownerId")
    Long totalDownloadsByOwner(@Param("ownerId") UUID ownerId);
}
