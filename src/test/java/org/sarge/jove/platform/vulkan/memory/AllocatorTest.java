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
import org.sarge.jove.platform.vulkan.memory.Allocator.SimpleAllocator;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.ptr.PointerByReference;

public class AllocatorTest extends AbstractVulkanTest {
	private MemoryType type;

	@BeforeEach
	void before() {
		final Heap heap = new Heap(0, 0, Set.of());
		type = new MemoryType(1, heap, Set.of());
	}

	@Nested
	class SimpleAllocatorTests {
		private SimpleAllocator allocator;

		@BeforeEach
		void before() {
			allocator = new SimpleAllocator(dev);
		}

		@Test
		void allocate() {
			// Allocate memory
			final DeviceMemory mem = allocator.allocate(type, 2);
			assertNotNull(mem);

			// Check memory block
			assertEquals(2, mem.size());
			assertEquals(false, mem.isMapped());
			assertEquals(false, mem.isDestroyed());

			// Check API
			final ArgumentCaptor<VkMemoryAllocateInfo> captor = ArgumentCaptor.forClass(VkMemoryAllocateInfo.class);
			final PointerByReference ref = lib.factory().pointer();
			verify(lib).vkAllocateMemory(eq(dev.handle()), captor.capture(), isNull(), eq(ref));

			// Check memory descriptor
			final VkMemoryAllocateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(1, info.memoryTypeIndex);
			assertEquals(2L, info.allocationSize);
		}
	}

	@Nested
	class PagedAllocatorTests {
		private static final int PAGE = 3;

		private Allocator delegate;
		private Allocator allocator;

		@BeforeEach
		void before() {
			delegate = mock(Allocator.class);
			allocator = Allocator.paged(delegate, PAGE);
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
