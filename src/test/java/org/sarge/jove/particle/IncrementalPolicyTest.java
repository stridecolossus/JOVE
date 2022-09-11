package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class IncrementalPolicyTest {
	private GenerationPolicy policy;

	@BeforeEach
	void before() {
		policy = new IncrementGenerationPolicy(1);
	}

	@DisplayName("The number of new particles is generated for each frame")
	@Test
	void count() {
		assertEquals(1, policy.count(0, 1));
	}

	@DisplayName("The number of new particles is accumulated as a fraction")
	@Test
	void accumulate() {
		assertEquals(0, policy.count(0, 0.5f));
		assertEquals(1, policy.count(0, 0.5f));
	}
}
