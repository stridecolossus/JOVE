package org.sarge.jove.platform.vulkan.memory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class AllocatorTest extends AbstractVulkanTest {
	private MemoryType type;

	@BeforeEach
	void before() {
		final Heap heap = new Heap(0, 0, Set.of());
		type = new MemoryType(1, heap, Set.of());
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
