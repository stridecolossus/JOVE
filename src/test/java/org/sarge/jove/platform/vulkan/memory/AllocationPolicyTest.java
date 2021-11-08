package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

public class AllocationPolicyTest {
	@Test
	void none() {
		assertEquals(1, AllocationPolicy.NONE.apply(1, 2));
	}

	@Test
	void initial() {
		final AllocationPolicy policy = AllocationPolicy.initial(3);
		assertEquals(3, policy.apply(1, 0));
		assertEquals(1, policy.apply(1, 2));
	}

	@Test
	void literal() {
		final AllocationPolicy policy = AllocationPolicy.literal(3);
		assertNotNull(policy);
		assertEquals(3, policy.apply(1, 2));
	}

	@Test
	void expand() {
		final AllocationPolicy policy = AllocationPolicy.expand(3);
		assertNotNull(policy);
		assertEquals(2 * 3, policy.apply(1, 2));
	}

	@Test
	void then() {
		final AllocationPolicy policy = mock(AllocationPolicy.class);
		final AllocationPolicy chain = AllocationPolicy.literal(1).then(policy);
		assertNotNull(chain);
		chain.apply(0, 2);
		verify(policy).apply(1, 2);
	}
}
