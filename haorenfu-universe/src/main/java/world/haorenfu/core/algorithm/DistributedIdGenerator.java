/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      DISTRIBUTED ID GENERATOR
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Advanced distributed unique ID generation based on Snowflake algorithm
 * with improvements for clock synchronization and efficiency.
 *
 * ID Structure (64 bits):
 * ┌─────────────┬──────────┬─────────┬──────────────┐
 * │ 1 bit sign  │ 41 bits  │ 10 bits │  12 bits     │
 * │   (unused)  │timestamp │worker ID│  sequence    │
 * └─────────────┴──────────┴─────────┴──────────────┘
 *
 * Mathematical Properties:
 * - Total IDs per millisecond per worker: 4096
 * - Total workers: 1024
 * - Time range: ~69 years from epoch
 * - Monotonically increasing within same millisecond
 *
 * Improvements over original Snowflake:
 * - Clock drift detection and compensation
 * - Spin-wait optimization for high throughput
 * - Statistical monitoring and reporting
 */
package world.haorenfu.core.algorithm;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe distributed unique ID generator.
 */
public class DistributedIdGenerator {

    // Bit allocation
    private static final long TIMESTAMP_BITS = 41L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    // Maximum values
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);  // 1023
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);    // 4095

    // Bit shifts
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    // Custom epoch (2024-01-01 00:00:00 UTC)
    private static final long EPOCH = 1704067200000L;

    // Instance fields
    private final long workerId;
    private final ReentrantLock lock = new ReentrantLock();

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    // Statistics
    private final AtomicLong totalGenerated = new AtomicLong(0);
    private final AtomicLong clockDriftCount = new AtomicLong(0);
    private final AtomicLong waitCount = new AtomicLong(0);

    /**
     * Creates a new ID generator for the specified worker.
     *
     * @param workerId Worker ID (0 to 1023)
     * @throws IllegalArgumentException if workerId is out of range
     */
    public DistributedIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                "Worker ID must be between 0 and " + MAX_WORKER_ID + ", got: " + workerId
            );
        }
        this.workerId = workerId;
    }

    /**
     * Generates the next unique ID.
     *
     * Time Complexity: O(1) amortized
     * Space Complexity: O(1)
     *
     * @return A unique 64-bit ID
     * @throws IllegalStateException if clock moves backwards beyond tolerance
     */
    public long nextId() {
        lock.lock();
        try {
            long currentTimestamp = currentTimeMillis();

            // Handle clock drift
            if (currentTimestamp < lastTimestamp) {
                long drift = lastTimestamp - currentTimestamp;
                clockDriftCount.incrementAndGet();

                // Allow up to 5ms drift with spin-wait
                if (drift <= 5) {
                    currentTimestamp = waitForNextMillis(lastTimestamp);
                } else {
                    throw new IllegalStateException(
                        "Clock moved backwards by " + drift + "ms. " +
                        "Refusing to generate ID to maintain ordering."
                    );
                }
            }

            // Same millisecond - increment sequence
            if (currentTimestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;

                // Sequence overflow - wait for next millisecond
                if (sequence == 0) {
                    waitCount.incrementAndGet();
                    currentTimestamp = waitForNextMillis(lastTimestamp);
                }
            } else {
                // New millisecond - reset sequence
                // Use a pseudo-random starting point to reduce collision probability
                // when multiple workers start at the same millisecond
                sequence = currentTimestamp & 0x3; // 0-3 based on timestamp
            }

            lastTimestamp = currentTimestamp;
            totalGenerated.incrementAndGet();

            // Compose the ID
            return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                 | (workerId << WORKER_ID_SHIFT)
                 | sequence;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Generates multiple IDs efficiently.
     *
     * @param count Number of IDs to generate
     * @return Array of unique IDs
     */
    public long[] nextIds(int count) {
        if (count <= 0) return new long[0];

        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = nextId();
        }
        return ids;
    }

    /**
     * Parses an ID into its components.
     */
    public IdComponents parse(long id) {
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        long parsedWorkerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long parsedSequence = id & MAX_SEQUENCE;

        return new IdComponents(
            id,
            Instant.ofEpochMilli(timestamp),
            parsedWorkerId,
            parsedSequence
        );
    }

    /**
     * Validates if an ID could have been generated by this system.
     */
    public boolean isValid(long id) {
        if (id <= 0) return false;

        IdComponents components = parse(id);

        // Check timestamp is reasonable (not in future, not before epoch)
        long now = System.currentTimeMillis();
        long idTime = components.timestamp.toEpochMilli();

        return idTime >= EPOCH && idTime <= now + 1000; // Allow 1s for clock skew
    }

    /**
     * Gets generation statistics.
     */
    public Statistics getStatistics() {
        return new Statistics(
            totalGenerated.get(),
            clockDriftCount.get(),
            waitCount.get(),
            workerId
        );
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                         HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Spin-waits until the next millisecond.
     * Uses exponential backoff to reduce CPU usage.
     */
    private long waitForNextMillis(long lastTimestamp) {
        long currentTimestamp = currentTimeMillis();
        int spins = 0;

        while (currentTimestamp <= lastTimestamp) {
            spins++;

            // Exponential backoff: yield after 10 spins, then brief sleep
            if (spins > 10) {
                Thread.yield();
            }
            if (spins > 100) {
                try {
                    Thread.sleep(0, 100); // 100 nanoseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            currentTimestamp = currentTimeMillis();
        }

        return currentTimestamp;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                         DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Parsed components of an ID.
     */
    public record IdComponents(
        long originalId,
        Instant timestamp,
        long workerId,
        long sequence
    ) {
        @Override
        public String toString() {
            return String.format(
                "ID[%d] = timestamp: %s, worker: %d, sequence: %d",
                originalId, timestamp, workerId, sequence
            );
        }
    }

    /**
     * Generator statistics.
     */
    public record Statistics(
        long totalGenerated,
        long clockDriftEvents,
        long sequenceWaits,
        long workerId
    ) {
        public double driftRate() {
            return totalGenerated == 0 ? 0 : (double) clockDriftEvents / totalGenerated;
        }

        public double waitRate() {
            return totalGenerated == 0 ? 0 : (double) sequenceWaits / totalGenerated;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                      UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a generator with auto-detected worker ID.
     * Uses a hash of the hostname and process ID.
     */
    public static DistributedIdGenerator createAutoConfigured() {
        long workerId = generateWorkerId();
        return new DistributedIdGenerator(workerId);
    }

    private static long generateWorkerId() {
        try {
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            long pid = ProcessHandle.current().pid();

            // Combine hostname hash and PID
            int hash = hostname.hashCode() ^ Long.hashCode(pid);
            return Math.abs(hash) % (MAX_WORKER_ID + 1);

        } catch (Exception e) {
            // Fallback to random
            return (long) (Math.random() * (MAX_WORKER_ID + 1));
        }
    }

    /**
     * Converts ID to a URL-safe base62 string.
     * Reduces ID length from 19 digits to ~11 characters.
     */
    public static String toBase62(long id) {
        if (id == 0) return "0";

        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();

        while (id > 0) {
            sb.append(chars.charAt((int) (id % 62)));
            id /= 62;
        }

        return sb.reverse().toString();
    }

    /**
     * Converts base62 string back to ID.
     */
    public static long fromBase62(String base62) {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        long id = 0;

        for (char c : base62.toCharArray()) {
            id = id * 62 + chars.indexOf(c);
        }

        return id;
    }
}
