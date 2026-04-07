package com.sankalp.marketservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PriceConstraintEngineTest {

	@Test
	void shouldCapPerSecondMovement() {
		double next = PriceConstraintEngine.nextPrice(100.0, 100.0, 5.0, 20.0, 12.5);
		assertEquals(105.0, next);
	}

	@Test
	void shouldCapDailyPositiveMovement() {
		double next = PriceConstraintEngine.nextPrice(119.0, 100.0, 5.0, 20.0, 3.0);
		assertEquals(120.0, next);
	}

	@Test
	void shouldCapDailyNegativeMovement() {
		double next = PriceConstraintEngine.nextPrice(81.0, 100.0, 5.0, 20.0, -3.0);
		assertEquals(80.0, next);
	}

	@Test
	void shouldNeverDropBelowOne() {
		double next = PriceConstraintEngine.nextPrice(1.2, 10.0, 5.0, 20.0, -5.0);
		assertTrue(next >= 1.0);
	}
}