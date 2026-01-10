/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      FRIENDSHIP REPOSITORY
 * ═══════════════════════════════════════════════════════════════════════════
 */
package world.haorenfu.domain.social;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import world.haorenfu.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for friendship operations.
 */
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Optional<Friendship> findByUserAndFriend(User user, User friend);

    Optional<Friendship> findByUserAndFriendAndStatus(User user, User friend, Friendship.FriendshipStatus status);

    List<Friendship> findAllByUserAndStatus(User user, Friendship.FriendshipStatus status);

    List<Friendship> findAllByFriendAndStatus(User friend, Friendship.FriendshipStatus status);

    List<Friendship> findAllByStatus(Friendship.FriendshipStatus status);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.user = :user AND f.status = 'ACCEPTED'")
    int countFriends(User user);

    @Modifying
    void deleteByUserAndFriend(User user, User friend);

    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.user = :user1 AND f.friend = :user2) OR " +
           "(f.user = :user2 AND f.friend = :user1)")
    List<Friendship> findBetweenUsers(User user1, User user2);
}
