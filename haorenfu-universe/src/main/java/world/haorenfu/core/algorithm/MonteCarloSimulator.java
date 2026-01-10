/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      MONTE CARLO SIMULATOR
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Monte Carlo simulation for probabilistic predictions and risk assessment.
 * Used for predicting server load, event outcomes, and statistical analysis.
 *
 * Mathematical Foundation:
 * Monte Carlo methods rely on repeated random sampling to obtain numerical
 * results. The underlying concept is to use randomness to solve problems
 * that might be deterministic in principle.
 *
 * Law of Large Numbers:
 *   As n → ∞, (1/n)Σf(xᵢ) → E[f(X)]
 *
 * Central Limit Theorem:
 *   The distribution of sample means approaches a normal distribution
 *   as the sample size increases, regardless of the population distribution.
 *
 * Applications:
 * - Server load prediction
 * - Event success probability
 * - Risk assessment
 * - A/B testing statistical significance
 */
package world.haorenfu.core.algorithm;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.DoubleSupplier;
import java.util.stream.DoubleStream;

/**
 * Monte Carlo simulation engine for probabilistic analysis.
 */
public class MonteCarloSimulator {

    private final Random random;
    private final ExecutorService executor;

    /**
     * Creates a Monte Carlo simulator.
     */
    public MonteCarloSimulator() {
        this.random = new Random();
        this.executor = Executors.newWorkStealingPool();
    }

    /**
     * Creates a simulator with a specific seed for reproducibility.
     */
    public MonteCarloSimulator(long seed) {
        this.random = new Random(seed);
        this.executor = Executors.newWorkStealingPool();
    }

    /**
     * Runs a simulation with the given probability function.
     *
     * @param trials Number of trials to run
     * @param simulation Function that returns a random sample
     * @return Simulation results with statistics
     */
    public SimulationResult simulate(int trials, DoubleSupplier simulation) {
        double[] results = new double[trials];

        for (int i = 0; i < trials; i++) {
            results[i] = simulation.getAsDouble();
        }

        return analyzeResults(results);
    }

    /**
     * Runs a parallel simulation for better performance.
     *
     * @param trials Number of trials to run
     * @param simulation Thread-safe function that returns a random sample
     * @return Simulation results with statistics
     */
    public SimulationResult simulateParallel(int trials, DoubleSupplier simulation) {
        double[] results = DoubleStream.generate(simulation)
            .parallel()
            .limit(trials)
            .toArray();

        return analyzeResults(results);
    }

    /**
     * Estimates probability of an event using simulation.
     *
     * @param trials Number of trials
     * @param eventOccurs Returns true if event occurs in this trial
     * @return Estimated probability with confidence interval
     */
    public ProbabilityEstimate estimateProbability(int trials, java.util.function.BooleanSupplier eventOccurs) {
        int successes = 0;

        for (int i = 0; i < trials; i++) {
            if (eventOccurs.getAsBoolean()) {
                successes++;
            }
        }

        double p = (double) successes / trials;

        // Wilson score interval for confidence bounds
        double z = 1.96; // 95% confidence
        double n = trials;
        double denominator = 1 + z * z / n;
        double center = (p + z * z / (2 * n)) / denominator;
        double margin = z * Math.sqrt((p * (1 - p) + z * z / (4 * n)) / n) / denominator;

        return new ProbabilityEstimate(p, center - margin, center + margin, trials);
    }

    /**
     * Performs A/B test analysis using Monte Carlo simulation.
     *
     * @param controlSuccesses Successes in control group
     * @param controlTotal Total in control group
     * @param treatmentSuccesses Successes in treatment group
     * @param treatmentTotal Total in treatment group
     * @param simulations Number of simulations
     * @return A/B test results
     */
    public ABTestResult analyzeABTest(
            int controlSuccesses, int controlTotal,
            int treatmentSuccesses, int treatmentTotal,
            int simulations) {

        double controlRate = (double) controlSuccesses / controlTotal;
        double treatmentRate = (double) treatmentSuccesses / treatmentTotal;
        double observedLift = (treatmentRate - controlRate) / controlRate;

        // Beta distributions for Bayesian analysis
        // Using Monte Carlo to estimate P(treatment > control)
        int treatmentBetter = 0;
        double[] liftDistribution = new double[simulations];

        for (int i = 0; i < simulations; i++) {
            // Sample from beta distributions
            double controlSample = sampleBeta(controlSuccesses + 1, controlTotal - controlSuccesses + 1);
            double treatmentSample = sampleBeta(treatmentSuccesses + 1, treatmentTotal - treatmentSuccesses + 1);

            if (treatmentSample > controlSample) {
                treatmentBetter++;
            }

            liftDistribution[i] = (treatmentSample - controlSample) / controlSample;
        }

        double probabilityBetter = (double) treatmentBetter / simulations;

        SimulationResult liftStats = analyzeResults(liftDistribution);

        return new ABTestResult(
            controlRate,
            treatmentRate,
            observedLift,
            probabilityBetter,
            liftStats.percentile(5),
            liftStats.percentile(95)
        );
    }

    /**
     * Predicts server load using time series Monte Carlo.
     *
     * @param historicalData Historical load values
     * @param periodsAhead How many periods to predict
     * @param simulations Number of simulations
     * @return Prediction with confidence intervals
     */
    public LoadPrediction predictServerLoad(double[] historicalData, int periodsAhead, int simulations) {
        // Calculate statistics from historical data
        double mean = Arrays.stream(historicalData).average().orElse(0);
        double variance = Arrays.stream(historicalData)
            .map(x -> (x - mean) * (x - mean))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // Run simulations
        double[][] predictions = new double[simulations][periodsAhead];

        for (int sim = 0; sim < simulations; sim++) {
            double lastValue = historicalData[historicalData.length - 1];

            for (int t = 0; t < periodsAhead; t++) {
                // Mean-reverting random walk
                double drift = 0.1 * (mean - lastValue);
                double shock = random.nextGaussian() * stdDev;
                lastValue = Math.max(0, lastValue + drift + shock);
                predictions[sim][t] = lastValue;
            }
        }

        // Calculate statistics for each period
        double[] predictedMeans = new double[periodsAhead];
        double[] lowerBounds = new double[periodsAhead];
        double[] upperBounds = new double[periodsAhead];

        for (int t = 0; t < periodsAhead; t++) {
            final int period = t;
            double[] periodValues = new double[simulations];
            for (int sim = 0; sim < simulations; sim++) {
                periodValues[sim] = predictions[sim][period];
            }
            Arrays.sort(periodValues);

            predictedMeans[t] = Arrays.stream(periodValues).average().orElse(0);
            lowerBounds[t] = periodValues[(int) (simulations * 0.05)];
            upperBounds[t] = periodValues[(int) (simulations * 0.95)];
        }

        return new LoadPrediction(predictedMeans, lowerBounds, upperBounds);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Samples from a Beta distribution using the Gamma method.
     */
    private double sampleBeta(double alpha, double beta) {
        double x = sampleGamma(alpha);
        double y = sampleGamma(beta);
        return x / (x + y);
    }

    /**
     * Samples from a Gamma distribution using Marsaglia and Tsang's method.
     */
    private double sampleGamma(double shape) {
        if (shape < 1) {
            return sampleGamma(shape + 1) * Math.pow(random.nextDouble(), 1.0 / shape);
        }

        double d = shape - 1.0 / 3.0;
        double c = 1.0 / Math.sqrt(9.0 * d);

        while (true) {
            double x, v;
            do {
                x = random.nextGaussian();
                v = 1.0 + c * x;
            } while (v <= 0);

            v = v * v * v;
            double u = random.nextDouble();

            if (u < 1.0 - 0.0331 * (x * x) * (x * x)) {
                return d * v;
            }

            if (Math.log(u) < 0.5 * x * x + d * (1.0 - v + Math.log(v))) {
                return d * v;
            }
        }
    }

    /**
     * Analyzes simulation results.
     */
    private SimulationResult analyzeResults(double[] results) {
        Arrays.sort(results);

        double sum = 0;
        double sumSquares = 0;

        for (double v : results) {
            sum += v;
            sumSquares += v * v;
        }

        int n = results.length;
        double mean = sum / n;
        double variance = (sumSquares / n) - (mean * mean);
        double stdDev = Math.sqrt(variance);

        double median = results[n / 2];
        double min = results[0];
        double max = results[n - 1];

        return new SimulationResult(results, mean, stdDev, median, min, max);
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executor.shutdown();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Result Records
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Simulation results with comprehensive statistics.
     */
    public record SimulationResult(
        double[] sortedResults,
        double mean,
        double stdDev,
        double median,
        double min,
        double max
    ) {
        /**
         * Gets the value at a specific percentile.
         */
        public double percentile(double p) {
            int index = (int) Math.ceil(p / 100.0 * sortedResults.length) - 1;
            return sortedResults[Math.max(0, Math.min(index, sortedResults.length - 1))];
        }

        /**
         * Gets the 95% confidence interval.
         */
        public double[] confidenceInterval95() {
            return new double[] { percentile(2.5), percentile(97.5) };
        }
    }

    /**
     * Probability estimate with confidence bounds.
     */
    public record ProbabilityEstimate(
        double probability,
        double lowerBound,
        double upperBound,
        int trials
    ) {
        /**
         * Checks if probability is statistically significant (different from 0.5).
         */
        public boolean isSignificant() {
            return upperBound < 0.5 || lowerBound > 0.5;
        }
    }

    /**
     * A/B test analysis results.
     */
    public record ABTestResult(
        double controlRate,
        double treatmentRate,
        double observedLift,
        double probabilityTreatmentBetter,
        double liftLowerBound,
        double liftUpperBound
    ) {
        /**
         * Checks if the result is statistically significant (>95% confidence).
         */
        public boolean isSignificant() {
            return probabilityTreatmentBetter > 0.95 || probabilityTreatmentBetter < 0.05;
        }

        /**
         * Returns a human-readable summary.
         */
        public String summary() {
            return String.format(
                "Control: %.2f%%, Treatment: %.2f%%, Lift: %.2f%% [%.2f%%, %.2f%%], P(better): %.1f%%",
                controlRate * 100, treatmentRate * 100, observedLift * 100,
                liftLowerBound * 100, liftUpperBound * 100, probabilityTreatmentBetter * 100
            );
        }
    }

    /**
     * Server load prediction results.
     */
    public record LoadPrediction(
        double[] predictedMeans,
        double[] lowerBounds,
        double[] upperBounds
    ) {
        /**
         * Gets prediction for a specific period.
         */
        public double getPrediction(int period) {
            return predictedMeans[period];
        }

        /**
         * Gets confidence interval for a specific period.
         */
        public double[] getConfidenceInterval(int period) {
            return new double[] { lowerBounds[period], upperBounds[period] };
        }
    }
}
