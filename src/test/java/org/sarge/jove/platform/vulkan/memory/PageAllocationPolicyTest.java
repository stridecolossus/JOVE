package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;

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
		final VulkanProperty granularity = mock(VulkanProperty.class);
		when(granularity.get()).thenReturn(5L);

		// Init property provider
		final VulkanProperty.Provider provider = mock(VulkanProperty.Provider.class);
		when(provider.property("bufferImageGranularity")).thenReturn(granularity);

		// Init device
		final DeviceContext dev = mock(DeviceContext.class);
		when(dev.provider()).thenReturn(provider);

		// Create paged policy
		policy = PageAllocationPolicy.of(dev);
		assertNotNull(policy);
		assertEquals(5, policy.apply(0, 0));
	}
}
