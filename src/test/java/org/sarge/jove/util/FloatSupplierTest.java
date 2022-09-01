package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class FloatSupplierTest {
	@Test
	void random() {
		final FloatSupplier random = FloatSupplier.of(new Random());
		final float f = random.get();
		assertTrue((f >= 0) && (f <= 1));
	}
}
