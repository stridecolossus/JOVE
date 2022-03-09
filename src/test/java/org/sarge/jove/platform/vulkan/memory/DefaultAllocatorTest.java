package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class DefaultAllocatorTest extends AbstractVulkanTest {
	private MemoryType type;

	@BeforeEach
	void before() {
		final Heap heap = new Heap(0, Set.of());
		type = new MemoryType(1, heap, Set.of());
	}

	@Test
	void allocate() {
		// Create default allocator
		final Allocator allocator = new DefaultAllocator(dev);
		assertNotNull(allocator);

		// Allocate memory
		final long size = 42;
		final DeviceMemory mem = allocator.allocate(type, size);
		assertNotNull(mem);
		assertEquals(size, mem.size());

		// Init expected descriptor
		final var expected = new VkMemoryAllocateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var info = (VkMemoryAllocateInfo) obj;
				assertNotNull(info);
				assertEquals(type.index(), info.memoryTypeIndex);
				assertEquals(size, info.allocationSize);
				return true;
			}
		};

		// Check API
		verify(lib).vkAllocateMemory(dev, expected, null, POINTER);
	}
}
