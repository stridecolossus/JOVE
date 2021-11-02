package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class BlockPolicyTest {
	@Test
	void none() {
		assertEquals(1, BlockPolicy.NONE.apply(1, 2));
	}

	@Test
	void literal() {
		final BlockPolicy policy = BlockPolicy.literal(3);
		assertNotNull(policy);
		assertEquals(3, policy.apply(1, 2));
	}

	@Test
	void expand() {
		final BlockPolicy policy = BlockPolicy.expand(3);
		assertNotNull(policy);
		assertEquals(2 * 3, policy.apply(1, 2));
	}
}
