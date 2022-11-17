package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class MemorySelectorTest extends AbstractVulkanTest {
	private MemorySelector selector;
	private MemoryType fallback, optimal;
	private VkMemoryRequirements reqs;
	private MemoryProperties.Builder<VkImageUsageFlag> props;

	@BeforeEach
	void before() {
		// Init request
		reqs = new VkMemoryRequirements();
		reqs.memoryTypeBits = 0b011;
		reqs.size = 3;

		// Init memory properties
		props = new MemoryProperties.Builder<>();
		props.usage(VkImageUsageFlag.COLOR_ATTACHMENT);

		// Create memory types
		final Heap heap = new Heap(0, Set.of());
		fallback = new MemoryType(0, heap, Set.of(VkMemoryProperty.HOST_VISIBLE));
		optimal = new MemoryType(1, heap, Set.of(VkMemoryProperty.HOST_VISIBLE, VkMemoryProperty.DEVICE_LOCAL));

		// Create selector
		selector = new MemorySelector(new MemoryType[]{fallback, optimal});
	}

	@DisplayName("The selector matches the memory type that contains the minimal properties if no optimal properties are specified")
	@Test
	void required() {
		props.required(VkMemoryProperty.HOST_VISIBLE);
		props.required(VkMemoryProperty.DEVICE_LOCAL);
		assertEquals(optimal, selector.select(reqs, props.build()));
	}

	@DisplayName("The selector matches the memory type that contains both the minimal and optimal properties when available")
	@Test
	void optimal() {
		props.required(VkMemoryProperty.HOST_VISIBLE);
		props.optimal(VkMemoryProperty.DEVICE_LOCAL);
		assertEquals(optimal, selector.select(reqs, props.build()));
	}

	@DisplayName("The selector falls back to the minimal properties if a memory type with the optimal properties is not available")
	@Test
	void fallback() {
		props.required(VkMemoryProperty.HOST_VISIBLE);
		props.optimal(VkMemoryProperty.HOST_COHERENT);
		assertEquals(fallback, selector.select(reqs, props.build()));
	}

	@DisplayName("The selector fails if there are no matching memory types")
	@Test
	void none() {
		props.required(VkMemoryProperty.PROTECTED);
		assertThrows(AllocationException.class, () -> selector.select(reqs, props.build()));
	}

	@DisplayName("The selector ignores memory types that are filtered out by the request")
	@Test
	void filter() {
		props.required(VkMemoryProperty.HOST_VISIBLE);
		reqs.memoryTypeBits = 0;
		assertThrows(AllocationException.class, () -> selector.select(reqs, props.build()));
	}
}
