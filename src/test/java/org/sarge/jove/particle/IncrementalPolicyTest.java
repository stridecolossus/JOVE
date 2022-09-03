package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class IncrementalPolicyTest {
	private GrowthPolicy policy;

	@BeforeEach
	void before() {
		policy = new IncrementalPolicy(1, 2);
	}

	@DisplayName("The number of new particles is generated for each frame")
	@Test
	void count() {
		assertEquals(1, policy.count(0, 1));
	}

	@DisplayName("The number of new particles is capped by the configured maximum")
	@Test
	void maximum() {
		assertEquals(2, policy.count(0, 3));
	}

	@DisplayName("The number of new particles is accumulated as a fraction")
	@Test
	void accumulate() {
		assertEquals(0, policy.count(0, 0.5f));
		assertEquals(1, policy.count(0, 0.5f));
	}

	@DisplayName("An accumulated number of particles is capped by the configured maximum")
	@Test
	void capped() {
		assertEquals(0, policy.count(0, 0.5f));
		assertEquals(2, policy.count(0, 3));
	}
}
