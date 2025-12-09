package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkImageUsageFlags;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class MemorySelectorTest {
	private MemorySelector selector;
	private MemoryType type;
	private MemoryProperties.Builder<?> properties;

	@BeforeEach
	void before() {
		properties = new MemoryProperties.Builder<>().usage(VkImageUsageFlags.SAMPLED);
		type = new MemoryType(0, new Heap(42, Set.of()), Set.of(DEVICE_LOCAL));
		selector = new MemorySelector(new MemoryType[]{type});
	}

	@DisplayName("The memory type matching the optimal properties is selected")
	@Test
	void optimal() {
		properties.optimal(DEVICE_LOCAL);
		assertEquals(Optional.of(type), selector.select(0x1, properties.build()));
	}

	@DisplayName("A fallback memory type is selected if none match the optimal requirements")
	void fallback() {
		properties.required(DEVICE_LOCAL);
		properties.optimal(PROTECTED);
		assertEquals(Optional.of(type), selector.select(0x1, properties.build()));
	}

	@DisplayName("No memory type is selected if none match the minimal requirements")
	@Test
	void none() {
		properties.required(HOST_COHERENT);
		assertEquals(Optional.empty(), selector.select(0x1, properties.build()));
	}

	@DisplayName("The candidate memory types are filtered by the allocation requirements mask")
	@Test
	void mask() {
		properties.required(DEVICE_LOCAL);
		assertEquals(Optional.empty(), selector.select(0x0, properties.build()));
	}
}
