package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.lib.util.Element;

public class IncrementGenerationPolicyTest {
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

	@Test
	void load() {
		final Element root = new Element("fixed", Map.of(), "3");
		policy = IncrementGenerationPolicy.load(root);
		assertEquals(3, policy.count(0, 1));
	}
}
