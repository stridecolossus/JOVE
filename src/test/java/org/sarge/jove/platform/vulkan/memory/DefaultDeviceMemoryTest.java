package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class DefaultDeviceMemoryTest {
	private DefaultDeviceMemory memory;
	private LogicalDevice device;
	private MockMemoryLibrary library;
	private MemoryType type;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
		library = new MockMemoryLibrary();
		device = new MockLogicalDevice(library);
		type = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryPropertyFlags.HOST_VISIBLE));
		memory = new DefaultDeviceMemory(new Handle(allocator.allocate(3)), device, type, 3);
	}

	@Test
	void constructor() {
		assertEquals(type, memory.type());
		assertEquals(3, memory.size());
		assertEquals(false, memory.isDestroyed());
	}

	@Test
	void equals() {
		assertEquals(memory, memory);
		assertNotEquals(memory, null);
		assertNotEquals(memory, new DefaultDeviceMemory(new Handle(allocator.allocate(3)), device, type, 3));
	}

	@DisplayName("A newly allocated device memory instance...")
	@Nested
	class Allocated {
		@DisplayName("is initially unmapped")
		@Test
		void region() {
			assertEquals(Optional.empty(), memory.region());
		}

		@DisplayName("can be mapped")
		@Test
		void map() {
			final Region region = memory.map(0, 3);
			assertEquals(3, region.size());
		}

		@DisplayName("cannot be mapped if it is not host visible")
		@Test
		void visible() {
			final MemoryType invalid = new MemoryType(0, new Heap(1, Set.of()), Set.of());
			memory = new DefaultDeviceMemory(new Handle(1), device, invalid, 3);
			assertThrows(IllegalStateException.class, () -> memory.map(0, memory.size()));
		}

		@DisplayName("cannot map a region larger than the memory")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> memory.map(1, 3));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			memory.destroy();
			assertEquals(true, memory.isDestroyed());
		}
	}

	@DisplayName("A mapped region of device memory...")
	@Nested
	class Mapped {
		private Region region;

		@BeforeEach
		void before() {
			region = memory.map(0, memory.size());
			assertEquals(3, region.size());
			assertEquals(Optional.of(region), memory.region());
		}

		@DisplayName("cannot be mapped more than once")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> memory.map(0, memory.size()));
		}

		@DisplayName("can be unmapped")
		@Test
		void unmap() {
			region.unmap();
			assertEquals(Optional.empty(), memory.region());
		}

		@DisplayName("can provide access to the underlying memory")
		@Test
		void buffer() {
			final MemorySegment segment = region.memory();
			assertEquals(false, segment.isReadOnly());
			assertEquals(3L, segment.byteSize());
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			memory.destroy();
			assertEquals(true, memory.isDestroyed());
			assertEquals(Optional.empty(), memory.region());
		}

		@Test
		void equals() {
			assertEquals(region, region);
			assertNotEquals(region, null);
		}
	}

	@DisplayName("An unmapped memory region...")
	@Nested
	class Unmapped {
		private Region region;

		@BeforeEach
		void before() {
			region = memory.map(0, memory.size());
			region.unmap();
		}

		@DisplayName("can be re-mapped")
		@Test
		void map() {
			memory.map(0, memory.size());
		}

		@DisplayName("cannot be unmapped more than once")
		@Test
		void unmap() {
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@DisplayName("cannot access the underlying memory segment")
		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> region.memory());
		}
	}

	@DisplayName("A destroyed memory instance...")
	@Nested
	class Destroyed {
		private Region region;

		@BeforeEach
		void before() {
			region = memory.map(0, memory.size());
			memory.destroy();
		}

		@DisplayName("cannot be mapped")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> memory.map(0, memory.size()));
		}

		@DisplayName("cannot be unmapped")
		@Test
		void unmap() {
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@DisplayName("is automatically unmapped")
		@Test
		void unmapped() {
			assertEquals(Optional.empty(), memory.region());
		}

		@DisplayName("cannot access the underlying memory segment")
		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> region.memory());
		}

		@DisplayName("cannot be destroyed again")
		@Test
		void destroy() {
			assertThrows(IllegalStateException.class, () -> memory.destroy());
		}
	}
}
