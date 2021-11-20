package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.memory.Allocator.DefaultAllocator;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocatorTest extends AbstractVulkanTest {
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

		// Check API
		final ArgumentCaptor<VkMemoryAllocateInfo> captor = ArgumentCaptor.forClass(VkMemoryAllocateInfo.class);
		verify(lib).vkAllocateMemory(eq(dev), captor.capture(), isNull(), eq(POINTER));

		// Check allocation descriptor
		final VkMemoryAllocateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(type.index(), info.memoryTypeIndex);
		assertEquals(size, info.allocationSize);
	}
}
