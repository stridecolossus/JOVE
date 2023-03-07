package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

public class AllocationServiceTest {
	private AllocationService service;
	private MemoryType type;
	private Allocator allocator;
	private VkMemoryRequirements reqs;
	private MemoryProperties<?> props;

	@BeforeEach
	void before() {
		// Init request
		reqs = new VkMemoryRequirements();
		props = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();

		// Init memory type
		type = new MemoryType(0, new MemoryType.Heap(0, Set.of()), Set.of());

		// Init selector
		final MemorySelector selector = mock(MemorySelector.class);
		when(selector.select(reqs, props)).thenReturn(type);

		// Create service
		allocator = mock(Allocator.class);
		service = new AllocationService(selector, allocator);
	}

	@Test
	void allocator() {
		assertEquals(allocator, service.allocator(props));
		assertEquals(allocator, service.allocator(null));
	}

	@Test
	void allocate() {
		reqs.size = 42;
		service.allocate(reqs, props);
		verify(allocator).allocate(type, 42);
	}
}
