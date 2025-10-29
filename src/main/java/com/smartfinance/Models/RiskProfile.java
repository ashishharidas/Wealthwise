package com.smartfinance.Models;

/**
 * Represents the user's tolerance for risk and provides utilities for
 * evaluating how well a stock fits a given profile.
 */
public enum RiskProfile {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE;

    /**
     * Calculates a suitability score (0-100) for a stock based on its
     * volatility and momentum. Higher scores indicate a better fit for the
     * profile.
     *
     * @param volatility annualised standard deviation of returns
     * @param momentum   price change over the analysis window (fractional)
     * @return suitability score in the range [0, 100]
     */
    public double calculateSuitabilityScore(double volatility, double momentum) {
        if (!Double.isFinite(volatility) || volatility < 0 || !Double.isFinite(momentum)) {
            return 0.0;
        }

        double lowVolatilityScore = clampToUnit(1.0 / (1.0 + volatility));
        double highVolatilityScore = clampToUnit(volatility / (volatility + 0.5));
        double balancedVolatilityScore = clampToUnit((lowVolatilityScore + highVolatilityScore) / 2.0);
        double momentumScore = clampToUnit((Math.tanh(momentum * 2.5) + 1.0) / 2.0);

        double rawScore;
        switch (this) {
            case CONSERVATIVE:
                rawScore = (0.7 * lowVolatilityScore) + (0.3 * momentumScore);
                break;
            case MODERATE:
                rawScore = (0.5 * balancedVolatilityScore) + (0.5 * momentumScore);
                break;
            case AGGRESSIVE:
                rawScore = (0.7 * highVolatilityScore) + (0.3 * momentumScore);
                break;
            default:
                rawScore = momentumScore;
        }

        return clampToUnit(rawScore) * 100.0;
    }

    private static double clampToUnit(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}