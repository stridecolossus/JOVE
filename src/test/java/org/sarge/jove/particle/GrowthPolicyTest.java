package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class GrowthPolicyTest {
	private float elapsed;

	@DisplayName("The NONE policy does not generate particles")
	@Test
	void none() {
		assertEquals(0, GrowthPolicy.NONE.count(0, elapsed));
		assertEquals(0, GrowthPolicy.NONE.count(1, elapsed));
	}

	@DisplayName("An incremental growth policy adds a constant number of particles")
	@Test
	void increment() {
		final GrowthPolicy policy = GrowthPolicy.increment(1);
		assertEquals(1, policy.count(0, elapsed));
		assertEquals(1, policy.count(1, elapsed));
	}

	@DisplayName("A fixed policy maintains a constant number of particles")
	@Test
	void max() {
		final GrowthPolicy policy = GrowthPolicy.fixed(2);
		assertEquals(2, policy.count(0, elapsed));
		assertEquals(1, policy.count(1, elapsed));
		assertEquals(0, policy.count(2, elapsed));
	}
}
