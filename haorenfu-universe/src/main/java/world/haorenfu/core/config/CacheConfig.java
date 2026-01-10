/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         CACHE CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * High-performance caching using Caffeine cache.
 * Optimized for different access patterns and data types.
 */
package world.haorenfu.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine cache configuration with optimized settings per cache type.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Register all cache names
        cacheManager.setCacheNames(Arrays.asList(
            "users", "posts", "skins", "serverStatus", "achievements"
        ));
        
        // Default cache specification
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats());

        return cacheManager;
    }

    /**
     * Specialized cache manager for user data with longer TTL.
     */
    @Bean("userCacheManager")
    public CacheManager userCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .recordStats());
        return cacheManager;
    }

    /**
     * Specialized cache manager for server status with short TTL.
     */
    @Bean("statusCacheManager")
    public CacheManager statusCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("serverStatus");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .recordStats());
        return cacheManager;
    }
}
