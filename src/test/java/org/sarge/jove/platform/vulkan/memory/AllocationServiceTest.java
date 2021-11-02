package org.sarge.jove.platform.vulkan.memory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsage;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocationServiceTest extends AbstractVulkanTest {
	private AllocationService service;
	private MemoryType type;
	private MemorySelector selector;
	private Allocator allocator;
	private VkMemoryRequirements reqs;
	private MemoryProperties<?> props;

	@BeforeEach
	void before() {
		// Init request
		reqs = new VkMemoryRequirements();
		props = new MemoryProperties.Builder<VkImageUsage>().usage(VkImageUsage.COLOR_ATTACHMENT).build();

		// Init selector
		type = new MemoryType(0, new MemoryType.Heap(0, 0, Set.of()), Set.of());
		selector = mock(MemorySelector.class);
		when(selector.select(reqs, props)).thenReturn(type);

		// Create service
		allocator = mock(Allocator.class);
		service = new AllocationService(selector, allocator);
	}

	@Test
	void allocate() {
		reqs.size = 42;
		service.allocate(reqs, props);
		verify(allocator).allocate(type, 42);
	}

	@Test
	void init() {
		final Allocator other = mock(Allocator.class);
		service.init(props, other);
		service.allocate(reqs, props);
		verify(other).allocate(type, 0);
	}
}
