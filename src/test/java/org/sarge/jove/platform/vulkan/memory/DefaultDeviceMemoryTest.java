package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class DefaultDeviceMemoryTest extends AbstractVulkanTest {
	private static final int SIZE = 3;

	private DefaultDeviceMemory mem;
	private Handle handle;

	@BeforeEach
	void before() {
		handle = new Handle(1);
		mem = new DefaultDeviceMemory(handle, dev, SIZE);
	}

	@Test
	void constructor() {
		assertEquals(handle, mem.handle());
		assertEquals(false, mem.isDestroyed());
		assertEquals(SIZE, mem.size());
	}

	@Test
	void equals() {
		assertEquals(mem, mem);
		assertEquals(mem, new DefaultDeviceMemory(handle, dev, SIZE));
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
			assertNotNull(mem.map());
		}

		@DisplayName("cannot be mapped if it is not host visible")
		@Test
		void visible() {
			final DeviceMemory invalid = new DefaultDeviceMemory(mem) {
				@Override
				public boolean isHostVisible() {
					return false;
				}
			};
			assertThrows(IllegalStateException.class, () -> invalid.map());
		}

		@DisplayName("cannot map a region larger than the memory")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> mem.map(1, SIZE));
		}

		@DisplayName("can be destroyed")
		@Test
		void destroy() {
			mem.destroy();
			assertEquals(true, mem.isDestroyed());
		}

		@DisplayName("cannot be reallocated by default")
		@Test
		void reallocate() {
			assertThrows(UnsupportedOperationException.class, () -> mem.reallocate());
		}
	}

	@DisplayName("A mapped device memory instance...")
	@Nested
	class Mapped {
		private Region region;

		@BeforeEach
		void before() {
			region = mem.map();
		}

		@DisplayName("has a region mapping")
		@Test
		void region() {
			assertNotNull(region);
			assertEquals(SIZE, region.size());
			assertEquals(Optional.of(region), mem.region());
		}

		@DisplayName("cannot be mapped more than once")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> mem.map());
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
			final ByteBuffer bb = region.buffer();
			assertNotNull(bb);
			assertEquals(SIZE, bb.capacity());
		}

		@DisplayName("cannot provide a buffer larger than the region")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> region.buffer(1, SIZE));
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
			region = mem.map();
			region.unmap();
		}

		@DisplayName("can be re-mapped")
		@Test
		void map() {
			mem.map();
		}

		@DisplayName("cannot be unmapped more than once")
		@Test
		void unmap() {
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@DisplayName("cannot provide a buffer")
		@Test
		void buffer() {
			assertThrows(IllegalStateException.class, () -> region.buffer());
		}
	}

	@DisplayName("A destroyed memory instance...")
	@Nested
	class Destroyed {
		private Region region;

		@BeforeEach
		void before() {
			region = mem.map();
			mem.destroy();
		}

		@DisplayName("cannot be mapped")
		@Test
		void map() {
			assertThrows(IllegalStateException.class, () -> mem.map());
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
			assertThrows(IllegalStateException.class, () -> region.buffer());
		}

		@DisplayName("cannot be destroyed again")
		@Test
		void destroy() {
			assertThrows(IllegalStateException.class, () -> mem.destroy());
		}
	}
}
