package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.DeviceLimits;

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

	@Test
	void of() {
		// Init page granularity
		final DeviceContext dev = mock(DeviceContext.class);
		final DeviceLimits limits = mock(DeviceLimits.class);
		when(limits.value("bufferImageGranularity")).thenReturn(5L);
		when(dev.limits()).thenReturn(limits);

		// Create paged policy
		policy = PageAllocationPolicy.of(dev);
		assertNotNull(policy);
		assertEquals(5, policy.apply(0, 0));
	}
}
