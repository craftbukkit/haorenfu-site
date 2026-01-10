/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      ANALYTICS ENGINE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Advanced statistical analysis for server metrics and user behavior.
 *
 * Mathematical Methods:
 * 1. Exponential Moving Average (EMA) for trend detection
 * 2. Z-Score anomaly detection
 * 3. Pearson correlation coefficient
 * 4. Simple linear regression for forecasting
 * 5. Percentile calculations
 * 6. Moving window statistics
 *
 * Applications:
 * - Server performance monitoring
 * - Player activity analysis
 * - Content engagement metrics
 * - Anomaly detection (bot detection, abuse prevention)
 */
package world.haorenfu.core.algorithm;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistical analytics engine for time-series data.
 */
public class AnalyticsEngine {

    // ═══════════════════════════════════════════════════════════════════════
    //                    BASIC STATISTICS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calculates basic descriptive statistics.
     */
    public static Statistics calculateStatistics(double[] values) {
        if (values == null || values.length == 0) {
            return new Statistics(0, 0, 0, 0, 0, 0, 0);
        }

        int n = values.length;
        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double v : values) {
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }

        double mean = sum / n;

        // Calculate variance using Welford's online algorithm (numerically stable)
        double m2 = 0;
        double delta;
        double runningMean = 0;

        for (int i = 0; i < n; i++) {
            delta = values[i] - runningMean;
            runningMean += delta / (i + 1);
            m2 += delta * (values[i] - runningMean);
        }

        double variance = n > 1 ? m2 / (n - 1) : 0;
        double stdDev = Math.sqrt(variance);

        // Calculate median
        double[] sorted = Arrays.copyOf(values, n);
        Arrays.sort(sorted);
        double median = n % 2 == 0
            ? (sorted[n / 2 - 1] + sorted[n / 2]) / 2
            : sorted[n / 2];

        return new Statistics(n, sum, mean, median, stdDev, min, max);
    }

    /**
     * Calculates percentile using linear interpolation.
     *
     * @param values Data values
     * @param p Percentile (0-100)
     * @return Percentile value
     */
    public static double percentile(double[] values, double p) {
        if (values == null || values.length == 0) return 0;
        if (p <= 0) return Arrays.stream(values).min().orElse(0);
        if (p >= 100) return Arrays.stream(values).max().orElse(0);

        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);

        double rank = p / 100.0 * (sorted.length - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        double weight = rank - lower;

        return sorted[lower] * (1 - weight) + sorted[upper] * weight;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //              EXPONENTIAL MOVING AVERAGE (EMA)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calculates Exponential Moving Average.
     *
     * EMA_t = α × X_t + (1 - α) × EMA_{t-1}
     *
     * where α = 2 / (span + 1)
     *
     * EMA gives more weight to recent observations, making it
     * responsive to recent changes while smoothing out noise.
     *
     * @param values Time series values
     * @param span Number of periods (determines smoothing factor)
     * @return EMA values
     */
    public static double[] exponentialMovingAverage(double[] values, int span) {
        if (values == null || values.length == 0) return new double[0];

        double alpha = 2.0 / (span + 1);
        double[] ema = new double[values.length];
        ema[0] = values[0];

        for (int i = 1; i < values.length; i++) {
            ema[i] = alpha * values[i] + (1 - alpha) * ema[i - 1];
        }

        return ema;
    }

    /**
     * Calculates Simple Moving Average.
     *
     * @param values Time series values
     * @param window Window size
     * @return SMA values (first window-1 values will be NaN)
     */
    public static double[] simpleMovingAverage(double[] values, int window) {
        if (values == null || values.length < window) return new double[0];

        double[] sma = new double[values.length];
        Arrays.fill(sma, 0, window - 1, Double.NaN);

        double sum = 0;
        for (int i = 0; i < window; i++) {
            sum += values[i];
        }
        sma[window - 1] = sum / window;

        for (int i = window; i < values.length; i++) {
            sum = sum - values[i - window] + values[i];
            sma[i] = sum / window;
        }

        return sma;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    ANOMALY DETECTION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Z-Score anomaly detector.
     *
     * Z = (X - μ) / σ
     *
     * Values with |Z| > threshold are considered anomalies.
     * Common thresholds: 2.0 (95%), 2.5 (99%), 3.0 (99.7%)
     *
     * @param values Data values
     * @param threshold Z-score threshold for anomaly
     * @return Indices of anomalous values
     */
    public static List<Integer> detectAnomaliesZScore(double[] values, double threshold) {
        if (values == null || values.length < 3) return Collections.emptyList();

        Statistics stats = calculateStatistics(values);
        if (stats.stdDev() == 0) return Collections.emptyList();

        List<Integer> anomalies = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double zScore = Math.abs((values[i] - stats.mean()) / stats.stdDev());
            if (zScore > threshold) {
                anomalies.add(i);
            }
        }

        return anomalies;
    }

    /**
     * Modified Z-Score using Median Absolute Deviation (MAD).
     * More robust to outliers than standard Z-score.
     *
     * Modified Z = 0.6745 × (X - median) / MAD
     *
     * @param values Data values
     * @param threshold Threshold (typically 3.5)
     * @return Indices of anomalous values
     */
    public static List<Integer> detectAnomaliesMAD(double[] values, double threshold) {
        if (values == null || values.length < 3) return Collections.emptyList();

        // Calculate median
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        double median = sorted.length % 2 == 0
            ? (sorted[sorted.length / 2 - 1] + sorted[sorted.length / 2]) / 2
            : sorted[sorted.length / 2];

        // Calculate MAD
        double[] deviations = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            deviations[i] = Math.abs(values[i] - median);
        }
        Arrays.sort(deviations);
        double mad = deviations.length % 2 == 0
            ? (deviations[deviations.length / 2 - 1] + deviations[deviations.length / 2]) / 2
            : deviations[deviations.length / 2];

        if (mad == 0) return Collections.emptyList();

        // Detect anomalies
        List<Integer> anomalies = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            double modifiedZ = 0.6745 * (values[i] - median) / mad;
            if (Math.abs(modifiedZ) > threshold) {
                anomalies.add(i);
            }
        }

        return anomalies;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    CORRELATION ANALYSIS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calculates Pearson correlation coefficient.
     *
     * r = Σ(x - x̄)(y - ȳ) / √(Σ(x - x̄)² × Σ(y - ȳ)²)
     *
     * Range: [-1, 1]
     *   1: Perfect positive correlation
     *   0: No correlation
     *  -1: Perfect negative correlation
     *
     * @param x First variable
     * @param y Second variable
     * @return Correlation coefficient
     */
    public static double pearsonCorrelation(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            return 0;
        }

        int n = x.length;
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        return denominator == 0 ? 0 : numerator / denominator;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    LINEAR REGRESSION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Simple linear regression using Ordinary Least Squares.
     *
     * Model: y = β₀ + β₁x
     *
     * β₁ = Σ(x - x̄)(y - ȳ) / Σ(x - x̄)²
     * β₀ = ȳ - β₁x̄
     *
     * @param x Independent variable
     * @param y Dependent variable
     * @return Regression result with coefficients and R²
     */
    public static LinearRegressionResult linearRegression(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length < 2) {
            return new LinearRegressionResult(0, 0, 0, 0, 0);
        }

        int n = x.length;
        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
        }
        double meanX = sumX / n;
        double meanY = sumY / n;

        double numerator = 0, denominator = 0;
        for (int i = 0; i < n; i++) {
            numerator += (x[i] - meanX) * (y[i] - meanY);
            denominator += (x[i] - meanX) * (x[i] - meanX);
        }

        double slope = denominator == 0 ? 0 : numerator / denominator;
        double intercept = meanY - slope * meanX;

        // Calculate R² (coefficient of determination)
        double ssRes = 0, ssTot = 0;
        for (int i = 0; i < n; i++) {
            double predicted = intercept + slope * x[i];
            ssRes += (y[i] - predicted) * (y[i] - predicted);
            ssTot += (y[i] - meanY) * (y[i] - meanY);
        }
        double rSquared = ssTot == 0 ? 0 : 1 - ssRes / ssTot;

        // Standard error of the estimate
        double standardError = Math.sqrt(ssRes / (n - 2));

        return new LinearRegressionResult(intercept, slope, rSquared, standardError, n);
    }

    /**
     * Forecasts future values using linear regression.
     *
     * @param values Historical values
     * @param steps Number of steps to forecast
     * @return Forecasted values
     */
    public static double[] forecast(double[] values, int steps) {
        if (values == null || values.length < 2 || steps <= 0) {
            return new double[0];
        }

        // Create x values (time indices)
        double[] x = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            x[i] = i;
        }

        LinearRegressionResult regression = linearRegression(x, values);

        // Generate forecasts
        double[] forecasts = new double[steps];
        for (int i = 0; i < steps; i++) {
            int futureX = values.length + i;
            forecasts[i] = regression.intercept() + regression.slope() * futureX;
        }

        return forecasts;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                    DATA CLASSES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Basic statistics result.
     */
    public record Statistics(
        int count,
        double sum,
        double mean,
        double median,
        double stdDev,
        double min,
        double max
    ) {
        public double variance() {
            return stdDev * stdDev;
        }

        public double range() {
            return max - min;
        }

        public double coefficientOfVariation() {
            return mean == 0 ? 0 : stdDev / mean;
        }
    }

    /**
     * Linear regression result.
     */
    public record LinearRegressionResult(
        double intercept,  // β₀
        double slope,      // β₁
        double rSquared,   // R²
        double standardError,
        int sampleSize
    ) {
        public double predict(double x) {
            return intercept + slope * x;
        }

        public double correlationCoefficient() {
            return Math.sqrt(rSquared) * (slope >= 0 ? 1 : -1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //                  TIME SERIES DATA POINT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Time-stamped data point for time series analysis.
     */
    public record TimeSeriesPoint(Instant timestamp, double value) {
        public static List<TimeSeriesPoint> create(Instant start, double[] values, Duration interval) {
            List<TimeSeriesPoint> points = new ArrayList<>();
            Instant current = start;
            for (double value : values) {
                points.add(new TimeSeriesPoint(current, value));
                current = current.plus(interval);
            }
            return points;
        }
    }

    /**
     * Aggregates time series data into buckets.
     */
    public static Map<Instant, Statistics> aggregate(
            List<TimeSeriesPoint> points,
            Duration bucketSize) {

        Map<Instant, List<Double>> buckets = new TreeMap<>();

        for (TimeSeriesPoint point : points) {
            long epochSeconds = point.timestamp().getEpochSecond();
            long bucketSeconds = bucketSize.getSeconds();
            long bucketStart = (epochSeconds / bucketSeconds) * bucketSeconds;
            Instant bucket = Instant.ofEpochSecond(bucketStart);

            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(point.value());
        }

        Map<Instant, Statistics> result = new LinkedHashMap<>();
        for (Map.Entry<Instant, List<Double>> entry : buckets.entrySet()) {
            double[] values = entry.getValue().stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
            result.put(entry.getKey(), calculateStatistics(values));
        }

        return result;
    }
}
