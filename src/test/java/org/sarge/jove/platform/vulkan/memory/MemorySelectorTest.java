package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsage;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class MemorySelectorTest extends AbstractVulkanTest {
	private MemorySelector selector;
	private MemoryType type;
	private VkMemoryRequirements reqs;
	private MemoryProperties.Builder<VkImageUsage> props;

	@BeforeEach
	void before() {
		// Init request
		reqs = new VkMemoryRequirements();
		reqs.memoryTypeBits = 1;
		reqs.size = 42;

		// Init memory properties
		props = new MemoryProperties.Builder<VkImageUsage>().usage(VkImageUsage.COLOR_ATTACHMENT);

		// Create memory type
		type = new MemoryType(0, new MemoryType.Heap(0, 0, Set.of()), Set.of(VkMemoryProperty.DEVICE_LOCAL));

		// Create selector
		selector = new MemorySelector(List.of(type));
	}

	@DisplayName("Select minimal memory type")
	@Test
	void allocate() {
		props.required(VkMemoryProperty.DEVICE_LOCAL);
		assertEquals(type, selector.select(reqs, props.build()));
	}

	@DisplayName("Select optimal memory type")
	@Test
	void allocateOptimal() {
		props.optimal(VkMemoryProperty.DEVICE_LOCAL);
		assertEquals(type, selector.select(reqs, props.build()));
	}

	@DisplayName("Fail if the memory type is filtered by the requirements")
	@Test
	void allocateTypeNotAvailable() {
		reqs.memoryTypeBits = 0;
		assertThrows(AllocationException.class, () -> selector.select(reqs, props.build()));
	}

	@DisplayName("Fail if there is no memory type with the required properties")
	@Test
	void allocatePropertyNotAvailable() {
		props.required(VkMemoryProperty.HOST_CACHED);
		assertThrows(AllocationException.class, () -> selector.select(reqs, props.build()));
	}
}
