package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.memory.Allocator.PageAllocator;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocatorTest extends AbstractVulkanTest {
	private MemoryType type;

	@BeforeEach
	void before() {
		final Heap heap = new Heap(0, 0, Set.of());
		type = new MemoryType(1, heap, Set.of());
	}

	@Test
	void allocate() {
		// Create default allocator
		final Allocator allocator = Allocator.allocator(dev);
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

	@Nested
	class PageAllocatorTests {
		private static final int PAGE = 3;

		private Allocator delegate;
		private Allocator allocator;

		@BeforeEach
		void before() {
			delegate = mock(Allocator.class);
			allocator = new PageAllocator(delegate, PAGE);
		}

		@Test
		void allocateSmaller() {
			allocator.allocate(type, 2);
			verify(delegate).allocate(type, PAGE);
		}

		@Test
		void allocatePageSize() {
			allocator.allocate(type, PAGE);
			verify(delegate).allocate(type, PAGE);
		}

		@Test
		void allocateLarger() {
			allocator.allocate(type, 4);
			verify(delegate).allocate(type, 2 * PAGE);
		}
	}
}
