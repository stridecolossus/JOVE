package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class DefaultAllocatorTest extends AbstractVulkanTest {
	private MemoryType type;
	private Allocator allocator;

	@BeforeEach
	void before() {
		final Heap heap = new Heap(0, Set.of());
		type = new MemoryType(1, heap, Set.of());
		allocator = new DefaultAllocator(dev);
	}

	@Test
	void allocate() {
		// Allocate memory
		final long size = 42;
		final DeviceMemory mem = allocator.allocate(type, size);
		assertNotNull(mem);
		assertEquals(size, mem.size());

		// Check API
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
		verify(lib).vkAllocateMemory(dev, expected, null, factory.pointer());
	}
}
