/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         BLOOM FILTER IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * A probabilistic data structure that provides space-efficient membership
 * testing. Originally conceived by Burton Howard Bloom in 1970, this
 * implementation uses modern insights from information theory.
 *
 * Mathematical Foundation:
 * - False positive probability: p ≈ (1 - e^(-kn/m))^k
 * - Optimal number of hash functions: k = (m/n) * ln(2)
 * - Required bits per element: m/n = -1.44 * log₂(p)
 *
 * Where:
 *   m = number of bits in the filter
 *   n = expected number of elements
 *   k = number of hash functions
 *   p = desired false positive probability
 *
 * Applications in this project:
 * - Username availability checking
 * - Duplicate content detection
 * - Spam filtering for forum posts
 * - Session token validation
 */
package world.haorenfu.core.algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe Bloom Filter with optimal parameter calculation.
 *
 * This implementation automatically calculates the optimal number of
 * hash functions based on the desired false positive rate, following
 * the mathematical principles established in the original paper.
 *
 * @param <T> The type of elements to be stored (must have meaningful toString)
 */
public class BloomFilter<T> {

    private final BitSet bitSet;
    private final int bitSetSize;
    private final int hashFunctionCount;
    private final ReadWriteLock lock;

    // Constants for hash function generation
    private static final long FNV_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;

    /**
     * Creates a new Bloom Filter with calculated optimal parameters.
     *
     * The formula for optimal bit array size:
     * m = -n * ln(p) / (ln(2))²
     *
     * The formula for optimal hash function count:
     * k = (m/n) * ln(2)
     *
     * @param expectedElements     Expected number of elements (n)
     * @param falsePositiveRate    Desired false positive probability (p)
     */
    public BloomFilter(int expectedElements, double falsePositiveRate) {
        // Calculate optimal bit array size using information theory
        // m = -n * ln(p) / (ln(2))²
        double ln2Squared = Math.pow(Math.log(2), 2);
        this.bitSetSize = (int) Math.ceil(
            -expectedElements * Math.log(falsePositiveRate) / ln2Squared
        );

        // Calculate optimal number of hash functions
        // k = (m/n) * ln(2)
        this.hashFunctionCount = Math.max(1, (int) Math.round(
            (double) bitSetSize / expectedElements * Math.log(2)
        ));

        this.bitSet = new BitSet(bitSetSize);
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Adds an element to the filter.
     *
     * Time Complexity: O(k) where k is the number of hash functions
     * Space Complexity: O(1) additional space
     *
     * @param element The element to add
     */
    public void add(T element) {
        if (element == null) return;

        lock.writeLock().lock();
        try {
            int[] hashes = computeHashes(element);
            for (int hash : hashes) {
                bitSet.set(Math.abs(hash % bitSetSize));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Tests if an element might be in the filter.
     *
     * Returns:
     * - true: Element might be present (with false positive probability p)
     * - false: Element is definitely not present (no false negatives)
     *
     * @param element The element to test
     * @return true if the element might be present
     */
    public boolean mightContain(T element) {
        if (element == null) return false;

        lock.readLock().lock();
        try {
            int[] hashes = computeHashes(element);
            for (int hash : hashes) {
                if (!bitSet.get(Math.abs(hash % bitSetSize))) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Computes k independent hash values for an element.
     *
     * Uses the double hashing technique: h_i(x) = h1(x) + i * h2(x)
     * This generates k hash functions from just two base hashes,
     * following the work of Kirsch and Mitzenmacher (2006).
     *
     * @param element The element to hash
     * @return Array of k hash values
     */
    private int[] computeHashes(T element) {
        byte[] data = element.toString().getBytes(StandardCharsets.UTF_8);

        // Compute two independent hash values
        long hash1 = fnv1aHash(data);
        long hash2 = murmurHash3(data);

        // Generate k hashes using double hashing
        int[] hashes = new int[hashFunctionCount];
        for (int i = 0; i < hashFunctionCount; i++) {
            hashes[i] = (int) ((hash1 + i * hash2) & 0x7FFFFFFF);
        }

        return hashes;
    }

    /**
     * FNV-1a hash function.
     *
     * A non-cryptographic hash function known for its speed and
     * excellent distribution properties.
     */
    private long fnv1aHash(byte[] data) {
        long hash = FNV_OFFSET_BASIS;
        for (byte b : data) {
            hash ^= (b & 0xFF);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    /**
     * Simplified MurmurHash3 implementation.
     *
     * MurmurHash3 was created by Austin Appleby and provides
     * excellent avalanche properties.
     */
    private long murmurHash3(byte[] data) {
        long h = 0x9747b28cL;
        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        for (byte b : data) {
            long k = b & 0xFF;
            k *= c1;
            k = Long.rotateLeft(k, 31);
            k *= c2;
            h ^= k;
            h = Long.rotateLeft(h, 27);
            h = h * 5 + 0x52dce729;
        }

        // Finalization mix
        h ^= data.length;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;

        return h;
    }

    /**
     * Returns the current fill ratio of the filter.
     *
     * As this ratio approaches 1, the false positive rate increases
     * beyond the designed threshold.
     *
     * @return Ratio of set bits to total bits
     */
    public double getFillRatio() {
        lock.readLock().lock();
        try {
            return (double) bitSet.cardinality() / bitSetSize;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Estimates the current false positive probability.
     *
     * Uses the formula: p ≈ (1 - e^(-k * n_est / m))^k
     * where n_est is estimated from the fill ratio.
     */
    public double estimatedFalsePositiveRate() {
        lock.readLock().lock();
        try {
            double fillRatio = (double) bitSet.cardinality() / bitSetSize;
            return Math.pow(fillRatio, hashFunctionCount);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears the filter, removing all elements.
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            bitSet.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns configuration information for monitoring.
     */
    public String getStats() {
        return String.format(
            "BloomFilter[bits=%d, hashFunctions=%d, fillRatio=%.4f, estimatedFPR=%.6f]",
            bitSetSize, hashFunctionCount, getFillRatio(), estimatedFalsePositiveRate()
        );
    }
}
