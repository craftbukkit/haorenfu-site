/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         RATE LIMITER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Simple in-memory rate limiter using token bucket algorithm.
 * Prevents abuse and ensures fair resource usage.
 */
package world.haorenfu.core.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter using token bucket algorithm.
 */
@Component
public class RateLimiter {

    // Default: 60 requests per minute
    private static final int DEFAULT_BUCKET_SIZE = 60;
    private static final long DEFAULT_REFILL_INTERVAL_MS = 1000; // 1 second

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Tries to consume a token for the given key.
     * 
     * @param key The identifier (e.g., IP address, user ID)
     * @return true if allowed, false if rate limited
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, DEFAULT_BUCKET_SIZE, DEFAULT_REFILL_INTERVAL_MS);
    }

    /**
     * Tries to consume a token with custom limits.
     */
    public boolean tryAcquire(String key, int bucketSize, long refillIntervalMs) {
        TokenBucket bucket = buckets.computeIfAbsent(key, 
            k -> new TokenBucket(bucketSize, refillIntervalMs));
        return bucket.tryConsume();
    }

    /**
     * Gets remaining tokens for a key.
     */
    public int getRemainingTokens(String key) {
        TokenBucket bucket = buckets.get(key);
        return bucket != null ? bucket.getAvailableTokens() : DEFAULT_BUCKET_SIZE;
    }

    /**
     * Clears all buckets (for testing or reset).
     */
    public void clear() {
        buckets.clear();
    }

    /**
     * Token bucket implementation.
     */
    private static class TokenBucket {
        private final int maxTokens;
        private final long refillIntervalMs;
        private int availableTokens;
        private Instant lastRefillTime;

        TokenBucket(int maxTokens, long refillIntervalMs) {
            this.maxTokens = maxTokens;
            this.refillIntervalMs = refillIntervalMs;
            this.availableTokens = maxTokens;
            this.lastRefillTime = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            return false;
        }

        synchronized int getAvailableTokens() {
            refill();
            return availableTokens;
        }

        private void refill() {
            Instant now = Instant.now();
            long elapsedMs = now.toEpochMilli() - lastRefillTime.toEpochMilli();
            int tokensToAdd = (int) (elapsedMs / refillIntervalMs);
            
            if (tokensToAdd > 0) {
                availableTokens = Math.min(maxTokens, availableTokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
