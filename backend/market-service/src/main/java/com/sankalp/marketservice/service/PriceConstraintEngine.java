package com.sankalp.marketservice.service;

public final class PriceConstraintEngine {

	private PriceConstraintEngine() {
	}

	public static double nextPrice(
			double currentPrice,
			double dayOpeningPrice,
			double maxPerSecond,
			double maxPerDay,
			double candidateSecondDelta) {

		double boundedSecondDelta = clamp(candidateSecondDelta, -maxPerSecond, maxPerSecond);
		double currentDayChange = currentPrice - dayOpeningPrice;

		double minAllowedDelta = -maxPerDay - currentDayChange;
		double maxAllowedDelta = maxPerDay - currentDayChange;
		double boundedDailyDelta = clamp(boundedSecondDelta, minAllowedDelta, maxAllowedDelta);

		double next = currentPrice + boundedDailyDelta;
		return Math.max(1.0, roundToTwoDecimals(next));
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private static double roundToTwoDecimals(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
