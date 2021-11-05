package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PageAllocationPolicyTest {
	private AllocationPolicy policy;

	@BeforeEach
	void before() {
		policy = new PageAllocationPolicy(5, 2);
	}

	@Test
	void apply() {
		assertEquals(2 * 5, policy.apply(0, 0));
		assertEquals(2 * 5, policy.apply(1, 0));
		assertEquals(2 * 5, policy.apply(5, 0));
		assertEquals(2 * 5, policy.apply(10, 0));
	}

	@Test
	void applyExceedsMinimum() {
		assertEquals(3 * 5, policy.apply(11, 0));
	}
}
