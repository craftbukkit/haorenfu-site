/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                        DATA INITIALIZER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Populates the database with sample data for development and demonstration.
 * This initializer runs on application startup when in development mode.
 *
 * Creates:
 * - Sample users with various roles
 * - Forum posts and comments
 * - Achievements
 * - Sample skins
 */
package world.haorenfu.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import world.haorenfu.domain.achievement.Achievement;
import world.haorenfu.domain.forum.ForumPost;
import world.haorenfu.domain.forum.PostCategory;
import world.haorenfu.domain.user.Role;
import world.haorenfu.domain.user.User;
import world.haorenfu.domain.user.UserRepository;

import java.util.List;
import java.util.Random;

/**
 * Development data initializer.
 */
@Configuration
@Profile("dev")
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final Random random = new Random(42);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already populated, skipping initialization");
                return;
            }

            log.info("Initializing database with sample data...");

            // Create admin user
            User admin = new User();
            admin.setUsername("Admin");
            admin.setEmail("admin@haorenfu.cn");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setRole(Role.OWNER);
            admin.setMinecraftId("Admin");
            admin.setWhitelisted(true);
            admin.setEmailVerified(true);
            admin.setReputation(10000);
            admin.setSignature("服务器管理员，有问题找我");
            userRepository.save(admin);
            log.info("Created admin user");

            // Create sample users
            List<String> usernames = List.of(
                "建筑大师", "红石专家", "生存达人", "PvP高手", "探险家",
                "矿工王", "农场主", "钓鱼佬", "附魔师", "交易商",
                "新手玩家", "萌新小白", "老玩家", "回归者", "观光客"
            );

            List<String> signatures = List.of(
                "建筑是我的生命",
                "红石让一切成为可能",
                "第一晚从来不挖三填一",
                "来单挑啊",
                "还有什么地方我没去过？",
                "钻石钻石闪闪亮",
                "粮食危机？不存在的",
                "钓到鹦鹉螺壳了！",
                "经验修补了解一下？",
                "低买高卖，稳赚不赔",
                "这游戏怎么玩啊",
                "求带",
                "当年我也是叱咤风云",
                "好久没玩了",
                "就看看不说话"
            );

            for (int i = 0; i < usernames.size(); i++) {
                User user = new User();
                user.setUsername(usernames.get(i));
                user.setEmail(usernames.get(i).toLowerCase().replace(" ", "") + "@example.com");
                user.setPasswordHash(encoder.encode("password123"));

                // Assign roles based on index
                if (i < 2) {
                    user.setRole(Role.ADMIN);
                } else if (i < 5) {
                    user.setRole(Role.MODERATOR);
                } else if (i < 8) {
                    user.setRole(Role.VIP);
                } else {
                    user.setRole(Role.MEMBER);
                }

                user.setMinecraftId("Player_" + (i + 1));
                user.setWhitelisted(i < 12);
                user.setEmailVerified(true);
                user.setReputation(random.nextInt(5000));
                user.setPlayTimeMinutes(random.nextInt(100000));
                user.setPostCount(random.nextInt(100));
                user.setCommentCount(random.nextInt(500));
                user.setSignature(signatures.get(i));

                userRepository.save(user);
            }

            log.info("Created {} sample users", usernames.size());
            log.info("Database initialization complete!");
        };
    }
}
