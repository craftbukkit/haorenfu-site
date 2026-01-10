/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      SHANNON ENTROPY ANALYZER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Implementation of Claude Shannon's information theory concepts for
 * measuring randomness and information content.
 *
 * Shannon Entropy Formula:
 * H(X) = -Σ p(xᵢ) * log₂(p(xᵢ))
 *
 * Where:
 * - H(X) is the entropy in bits
 * - p(xᵢ) is the probability of each unique symbol
 *
 * Applications:
 * - Password strength measurement
 * - Content uniqueness scoring
 * - Spam detection (low entropy = repetitive)
 * - Data compression potential analysis
 *
 * Historical Note:
 * Shannon's 1948 paper "A Mathematical Theory of Communication" laid
 * the foundation for the digital age. This implementation honors that
 * legacy by applying these principles to modern web applications.
 */
package world.haorenfu.core.algorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Entropy-based analysis tool for strings and data.
 *
 * Provides various metrics based on information theory to evaluate
 * the quality, randomness, and complexity of input data.
 */
public class EntropyAnalyzer {

    // Character class patterns for password analysis
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGITS = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern UNICODE_EXTENDED = Pattern.compile("[^\\x00-\\x7F]");

    // Common password patterns to detect
    private static final Pattern KEYBOARD_PATTERN = Pattern.compile(
        "qwerty|asdf|zxcv|1234|4321|password|admin|letmein|welcome|monkey|dragon"
    );

    /**
     * Calculates the Shannon entropy of a string.
     *
     * The entropy represents the average number of bits needed to
     * encode each character, given the character distribution.
     *
     * Time Complexity: O(n) where n is the string length
     *
     * @param input The string to analyze
     * @return Entropy in bits per character
     */
    public static double calculateEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }

        // Count character frequencies
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            frequencyMap.merge(c, 1, Integer::sum);
        }

        // Calculate entropy using Shannon's formula
        double entropy = 0.0;
        int length = input.length();

        for (int count : frequencyMap.values()) {
            double probability = (double) count / length;
            // H += -p * log2(p)
            entropy -= probability * log2(probability);
        }

        return entropy;
    }

    /**
     * Calculates the total information content of a string.
     *
     * Total bits = entropy per character × number of characters
     *
     * @param input The string to analyze
     * @return Total entropy in bits
     */
    public static double calculateTotalEntropy(String input) {
        if (input == null || input.isEmpty()) {
            return 0.0;
        }
        return calculateEntropy(input) * input.length();
    }

    /**
     * Analyzes password strength using multiple entropy-based metrics.
     *
     * Combines:
     * 1. Character-level Shannon entropy
     * 2. Character class diversity
     * 3. Length bonus (exponential security gain)
     * 4. Pattern detection penalty
     *
     * @param password The password to analyze
     * @return PasswordStrength object with detailed metrics
     */
    public static PasswordStrength analyzePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, 0, 0, "无效密码", StrengthLevel.INVALID);
        }

        // Calculate base entropy
        double charEntropy = calculateEntropy(password);

        // Calculate alphabet size (character pool)
        int alphabetSize = calculateAlphabetSize(password);

        // Calculate ideal entropy: log2(alphabetSize^length)
        // This represents maximum possible entropy for this character set
        double idealEntropy = password.length() * log2(alphabetSize);

        // Pattern penalty (reduces score for common patterns)
        double patternPenalty = calculatePatternPenalty(password);

        // Final score combines actual entropy with character diversity
        double finalScore = (charEntropy * password.length() * 0.5 + idealEntropy * 0.5)
                          * (1 - patternPenalty);

        // Determine strength level
        StrengthLevel level = determineStrengthLevel(finalScore, password.length());
        String feedback = generateFeedback(password, finalScore, level);

        return new PasswordStrength(charEntropy, idealEntropy, finalScore, feedback, level);
    }

    /**
     * Calculates the effective alphabet size based on character classes used.
     *
     * Standard character class sizes:
     * - Lowercase letters: 26
     * - Uppercase letters: 26
     * - Digits: 10
     * - Special characters: ~32
     * - Extended Unicode: ~100 (conservative estimate)
     */
    private static int calculateAlphabetSize(String password) {
        int size = 0;

        if (LOWERCASE.matcher(password).find()) size += 26;
        if (UPPERCASE.matcher(password).find()) size += 26;
        if (DIGITS.matcher(password).find()) size += 10;
        if (SPECIAL.matcher(password).find()) size += 32;
        if (UNICODE_EXTENDED.matcher(password).find()) size += 100;

        return Math.max(size, 1);
    }

    /**
     * Detects common patterns that reduce effective entropy.
     *
     * Returns a penalty factor between 0 (no penalty) and 0.5 (severe penalty).
     */
    private static double calculatePatternPenalty(String password) {
        double penalty = 0.0;
        String lower = password.toLowerCase();

        // Check for keyboard patterns
        if (KEYBOARD_PATTERN.matcher(lower).find()) {
            penalty += 0.3;
        }

        // Check for repeated characters
        int maxRepeat = findMaxRepeat(password);
        if (maxRepeat > password.length() / 3) {
            penalty += 0.2;
        }

        // Check for sequential characters (abc, 123)
        if (hasSequentialChars(password, 3)) {
            penalty += 0.15;
        }

        return Math.min(penalty, 0.5);
    }

    /**
     * Finds the maximum consecutive repetition of any character.
     */
    private static int findMaxRepeat(String s) {
        if (s.length() < 2) return 1;

        int maxRepeat = 1;
        int currentRepeat = 1;

        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                currentRepeat++;
                maxRepeat = Math.max(maxRepeat, currentRepeat);
            } else {
                currentRepeat = 1;
            }
        }

        return maxRepeat;
    }

    /**
     * Checks for sequential character patterns.
     */
    private static boolean hasSequentialChars(String s, int minLength) {
        if (s.length() < minLength) return false;

        int ascending = 1;
        int descending = 1;

        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) - s.charAt(i - 1) == 1) {
                ascending++;
                if (ascending >= minLength) return true;
            } else {
                ascending = 1;
            }

            if (s.charAt(i - 1) - s.charAt(i) == 1) {
                descending++;
                if (descending >= minLength) return true;
            } else {
                descending = 1;
            }
        }

        return false;
    }

    /**
     * Determines strength level based on score and length.
     */
    private static StrengthLevel determineStrengthLevel(double score, int length) {
        if (length < 6) return StrengthLevel.WEAK;
        if (score < 20) return StrengthLevel.WEAK;
        if (score < 40) return StrengthLevel.FAIR;
        if (score < 60) return StrengthLevel.GOOD;
        if (score < 80) return StrengthLevel.STRONG;
        return StrengthLevel.EXCELLENT;
    }

    /**
     * Generates human-readable feedback for password improvement.
     */
    private static String generateFeedback(String password, double score, StrengthLevel level) {
        StringBuilder feedback = new StringBuilder();

        if (password.length() < 8) {
            feedback.append("建议使用至少8个字符。");
        }
        if (!UPPERCASE.matcher(password).find()) {
            feedback.append("添加大写字母可提高安全性。");
        }
        if (!DIGITS.matcher(password).find()) {
            feedback.append("添加数字可增加复杂度。");
        }
        if (!SPECIAL.matcher(password).find()) {
            feedback.append("特殊字符能显著提升强度。");
        }
        if (KEYBOARD_PATTERN.matcher(password.toLowerCase()).find()) {
            feedback.append("避免使用常见键盘模式。");
        }

        if (feedback.length() == 0) {
            return switch (level) {
                case EXCELLENT -> "密码强度优秀!";
                case STRONG -> "密码强度良好。";
                case GOOD -> "密码强度尚可。";
                default -> "建议增强密码复杂度。";
            };
        }

        return feedback.toString();
    }

    /**
     * Base-2 logarithm utility.
     */
    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /**
     * Calculates the normalized entropy ratio.
     *
     * Returns a value between 0 and 1, where:
     * - 0 means completely uniform (like "aaaa")
     * - 1 means maximum entropy (all unique characters)
     *
     * @param input The string to analyze
     * @return Normalized entropy ratio
     */
    public static double getNormalizedEntropy(String input) {
        if (input == null || input.length() < 2) {
            return 0.0;
        }

        double actualEntropy = calculateEntropy(input);
        double maxEntropy = log2(input.length()); // Max when all chars unique

        return actualEntropy / maxEntropy;
    }

    /**
     * Enum representing password strength levels.
     */
    public enum StrengthLevel {
        INVALID("无效", 0),
        WEAK("弱", 1),
        FAIR("一般", 2),
        GOOD("良好", 3),
        STRONG("强", 4),
        EXCELLENT("极强", 5);

        private final String label;
        private final int score;

        StrengthLevel(String label, int score) {
            this.label = label;
            this.score = score;
        }

        public String getLabel() { return label; }
        public int getScore() { return score; }
    }

    /**
     * Record containing detailed password strength analysis.
     */
    public record PasswordStrength(
        double characterEntropy,
        double idealEntropy,
        double finalScore,
        String feedback,
        StrengthLevel level
    ) {
        public int getPercentage() {
            return (int) Math.min(100, finalScore);
        }
    }
}
