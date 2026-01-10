/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                         KALMAN FILTER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Implementation of the Kalman Filter for optimal state estimation.
 * Used for predicting server latency and smoothing noisy measurements.
 *
 * Mathematical Foundation:
 * The Kalman filter is an optimal recursive Bayesian estimator that uses
 * a series of measurements observed over time to produce estimates of
 * unknown variables that tend to be more accurate than single measurements.
 *
 * State Equations:
 *   Prediction:
 *     x̂ₖ|ₖ₋₁ = F·x̂ₖ₋₁|ₖ₋₁ + B·uₖ
 *     Pₖ|ₖ₋₁ = F·Pₖ₋₁|ₖ₋₁·Fᵀ + Q
 *
 *   Update:
 *     ỹₖ = zₖ - H·x̂ₖ|ₖ₋₁
 *     Sₖ = H·Pₖ|ₖ₋₁·Hᵀ + R
 *     Kₖ = Pₖ|ₖ₋₁·Hᵀ·Sₖ⁻¹
 *     x̂ₖ|ₖ = x̂ₖ|ₖ₋₁ + Kₖ·ỹₖ
 *     Pₖ|ₖ = (I - Kₖ·H)·Pₖ|ₖ₋₁
 *
 * Applications in this project:
 * - Server latency prediction and smoothing
 * - Player count trend estimation
 * - Resource usage forecasting
 */
package world.haorenfu.core.algorithm;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * One-dimensional Kalman Filter implementation.
 * Optimized for real-time server metrics prediction.
 */
public class KalmanFilter {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // State estimate
    private double stateEstimate;

    // Estimate uncertainty (error covariance)
    private double estimateUncertainty;

    // Process noise covariance (Q)
    private final double processNoise;

    // Measurement noise covariance (R)
    private final double measurementNoise;

    // Kalman gain
    private double kalmanGain;

    // Statistics
    private int measurementCount;
    private double sumSquaredError;

    /**
     * Creates a Kalman filter with specified noise parameters.
     *
     * @param initialEstimate Initial state estimate
     * @param initialUncertainty Initial estimate uncertainty
     * @param processNoise Process noise covariance (Q) - higher = more adaptive
     * @param measurementNoise Measurement noise covariance (R) - higher = smoother
     */
    public KalmanFilter(double initialEstimate, double initialUncertainty,
                        double processNoise, double measurementNoise) {
        this.stateEstimate = initialEstimate;
        this.estimateUncertainty = initialUncertainty;
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
        this.kalmanGain = 0.0;
        this.measurementCount = 0;
        this.sumSquaredError = 0.0;
    }

    /**
     * Factory method for latency estimation.
     * Tuned for typical server latency characteristics.
     */
    public static KalmanFilter forLatencyEstimation() {
        // Initial estimate: 50ms
        // Initial uncertainty: high (1000)
        // Process noise: moderate (1.0) - latency can change
        // Measurement noise: moderate (10.0) - measurements are somewhat noisy
        return new KalmanFilter(50.0, 1000.0, 1.0, 10.0);
    }

    /**
     * Factory method for player count estimation.
     * Tuned for smoother player count predictions.
     */
    public static KalmanFilter forPlayerCountEstimation() {
        return new KalmanFilter(0.0, 100.0, 0.5, 5.0);
    }

    /**
     * Performs prediction step (time update).
     * Call this before update() if there's a time gap.
     */
    public void predict() {
        lock.writeLock().lock();
        try {
            // For simple 1D case with no control input:
            // State estimate remains the same
            // Uncertainty increases by process noise
            estimateUncertainty += processNoise;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Performs update step (measurement update).
     *
     * @param measurement The observed measurement
     * @return Updated state estimate
     */
    public double update(double measurement) {
        lock.writeLock().lock();
        try {
            // Prediction step (implicit, assuming F = 1)
            estimateUncertainty += processNoise;

            // Calculate Kalman gain
            // K = P / (P + R)
            kalmanGain = estimateUncertainty / (estimateUncertainty + measurementNoise);

            // Innovation (measurement residual)
            double innovation = measurement - stateEstimate;

            // Update estimate
            // x̂ = x̂ + K * (z - x̂)
            stateEstimate += kalmanGain * innovation;

            // Update uncertainty
            // P = (1 - K) * P
            estimateUncertainty = (1.0 - kalmanGain) * estimateUncertainty;

            // Track statistics
            measurementCount++;
            sumSquaredError += innovation * innovation;

            return stateEstimate;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the current state estimate.
     */
    public double getEstimate() {
        lock.readLock().lock();
        try {
            return stateEstimate;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the current estimate uncertainty.
     */
    public double getUncertainty() {
        lock.readLock().lock();
        try {
            return estimateUncertainty;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the current Kalman gain.
     * Values close to 1 mean trusting measurements more.
     * Values close to 0 mean trusting predictions more.
     */
    public double getKalmanGain() {
        lock.readLock().lock();
        try {
            return kalmanGain;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Calculates the Root Mean Square Error.
     */
    public double getRMSE() {
        lock.readLock().lock();
        try {
            if (measurementCount == 0) return 0.0;
            return Math.sqrt(sumSquaredError / measurementCount);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the confidence interval for the estimate.
     *
     * @param sigmas Number of standard deviations (typically 1.96 for 95% CI)
     * @return Array of [lower bound, upper bound]
     */
    public double[] getConfidenceInterval(double sigmas) {
        lock.readLock().lock();
        try {
            double stdDev = Math.sqrt(estimateUncertainty);
            return new double[] {
                stateEstimate - sigmas * stdDev,
                stateEstimate + sigmas * stdDev
            };
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Resets the filter to initial state.
     */
    public void reset(double initialEstimate, double initialUncertainty) {
        lock.writeLock().lock();
        try {
            this.stateEstimate = initialEstimate;
            this.estimateUncertainty = initialUncertainty;
            this.kalmanGain = 0.0;
            this.measurementCount = 0;
            this.sumSquaredError = 0.0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Returns a summary of the filter state.
     */
    public FilterState getState() {
        lock.readLock().lock();
        try {
            double[] ci95 = getConfidenceInterval(1.96);
            return new FilterState(
                stateEstimate,
                estimateUncertainty,
                kalmanGain,
                measurementCount,
                getRMSE(),
                ci95[0],
                ci95[1]
            );
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Immutable state snapshot.
     */
    public record FilterState(
        double estimate,
        double uncertainty,
        double kalmanGain,
        int measurementCount,
        double rmse,
        double confidenceLower,
        double confidenceUpper
    ) {
        /**
         * Confidence level as percentage (0-100).
         */
        public double confidenceLevel() {
            if (uncertainty <= 0) return 100.0;
            // Higher uncertainty = lower confidence
            return Math.max(0, Math.min(100, 100.0 - uncertainty));
        }
    }
}
