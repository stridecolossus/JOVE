package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkImageUsageFlag;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocationRoutingServiceTest extends AbstractVulkanTest {
	private AllocationRoutingService service;
	private Allocator allocator;

	@BeforeEach
	void before() {
		allocator = mock(Allocator.class);
		service = new AllocationRoutingService(mock(MemorySelector.class), allocator);
	}

	@DisplayName("An unmatched request is routed to the default allocator")
	@Test
	void unmatched() {
		assertEquals(allocator, service.allocator(null));
	}

	@DisplayName("A matching request is routed to the specified allocator")
	@Test
	void route() {
		final MemoryProperties<?> props = new MemoryProperties.Builder<>().usage(VkImageUsageFlag.COLOR_ATTACHMENT).build();
		final Allocator other = mock(Allocator.class);
		service.route(props::equals, other);
		assertEquals(other, service.allocator(props));
	}
}
