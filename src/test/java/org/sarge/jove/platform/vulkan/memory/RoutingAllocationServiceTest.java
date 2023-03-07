package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

public class RoutingAllocationServiceTest {
	private RoutingAllocationService service;
	private Allocator def;

	@BeforeEach
	void before() {
		def = mock(Allocator.class);
		service = new RoutingAllocationService(mock(MemorySelector.class), def);
	}

	@DisplayName("An unmatched request is routed to the default allocator")
	@Test
	void unmatched() {
		assertEquals(def, service.allocator(null));
	}

	@DisplayName("A matching request is routed to the specified allocator")
	@Test
	void route() {
		final MemoryProperties<?> props = new MemoryProperties.Builder<>()
				.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();

		final Allocator other = mock(Allocator.class);
		service.route(props::equals, other);
		assertEquals(other, service.allocator(props));
	}
}
