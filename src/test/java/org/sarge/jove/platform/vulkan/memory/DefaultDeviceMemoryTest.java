package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

class DefaultDeviceMemoryTest {
	private DefaultDeviceMemory mem;
	private DeviceContext dev;
	private MemoryType type;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		type = new MemoryType(0, new Heap(1, Set.of()), Set.of(VkMemoryProperty.HOST_VISIBLE));
		mem = new DefaultDeviceMemory(new Handle(1), dev, type, 3);
	}

	@Test
	void constructor() {
		assertEquals(type, mem.type());
		assertEquals(3, mem.size());
		assertEquals(false, mem.isDestroyed());
	}

	@Test
	void equals() {
		assertEquals(mem, mem);
		assertEquals(mem, new DefaultDeviceMemory(new Handle(1), dev, type, 3));
		assertNotEquals(mem, null);
		assertNotEquals(mem, mock(DeviceMemory.class));
	}

	@DisplayName("A newly allocated device memory instance...")
	@Nested
	class Allocated {
		@DisplayName("is initially unmapped")
		@Test
		void region() {
			assertEquals(Optional.empty(), mem.region());
		}

		@DisplayName("can be mapped")
		@Test
		void map() {
			assertNotNull(mem.map(0, mem.size()));
		}

		@DisplayName("cannot be mapped if it is not host visible")
		@Test
		void visible() {
			final MemoryType invalid = new MemoryType(0, new Heap(1, Set.of()), Set.of());
			mem = new DefaultDeviceMemory(new Handle(1), dev, invalid, 3);
			assertThrows(IllegalStateException.class, () -> mem.map(0, mem.size()));
		}

		@DisplayName("cannot map a region larger than the memory")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> mem.map(1, 3));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			mem.destroy();
			assertEquals(true, mem.isDestroyed());
		}
	}

	@DisplayName("A mapped device memory instance...")
	@Nested
	class Mapped {
		private Region region;

		@BeforeEach
		void before() {
			region = mem.map(0, mem.size());
		}

		@DisplayName("has a region mapping")
		@Test
		void region() {
			assertEquals(3, region.size());
			assertEquals(Optional.of(region), mem.region());
		}

		@DisplayName("cannot be mapped more than once")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> mem.map(0, mem.size()));
		}

		@DisplayName("can be unmapped")
		@Test
		void unmap() {
			region.unmap();
			assertEquals(Optional.empty(), mem.region());
		}

		@DisplayName("can provide a buffer for read-write operations")
		@Test
		void buffer() {
			final ByteBuffer bb = region.buffer(0, region.size());
			assertEquals(3, bb.capacity());
		}

		@DisplayName("cannot provide a buffer larger than the region")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> region.buffer(1, 3));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			mem.destroy();
			assertEquals(true, mem.isDestroyed());
			assertEquals(Optional.empty(), mem.region());
		}

		@Test
		void equals() {
			assertEquals(region, region);
			assertNotEquals(region, null);
			assertNotEquals(region, mock(Region.class));
		}
	}

	@DisplayName("An unmapped memory region...")
	@Nested
	class Unmapped {
		private Region region;

		@BeforeEach
		void before() {
			region = mem.map(0, mem.size());
			region.unmap();
		}

		@DisplayName("can be re-mapped")
		@Test
		void map() {
			mem.map(0, mem.size());
		}

		@DisplayName("cannot be unmapped more than once")
		@Test
		void unmap() {
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@DisplayName("cannot provide a buffer")
		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> region.buffer(0, region.size()));
		}
	}

	@DisplayName("A destroyed memory instance...")
	@Nested
	class Destroyed {
		private Region region;

		@BeforeEach
		void before() {
			region = mem.map(0, mem.size());
			mem.destroy();
		}

		@DisplayName("cannot be mapped")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> mem.map(0, mem.size()));
		}

		@DisplayName("cannot be unmapped")
		@Test
		void unmap() {
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@DisplayName("is automatically unmapped")
		@Test
		void unmapped() {
			assertEquals(Optional.empty(), mem.region());
		}

		@DisplayName("cannot provide a buffer")
		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> region.buffer(0, region.size()));
		}

		@DisplayName("cannot be destroyed again")
		@Test
		void destroy() {
			assertThrows(IllegalStateException.class, () -> mem.destroy());
		}
	}
}
