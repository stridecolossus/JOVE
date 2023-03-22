package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.memory.Block.BlockDeviceMemory;

public class BlockTest {
	private Block block;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		mem = new DefaultDeviceMemory(new Handle(1), new MockDeviceContext(), MockAllocator.TYPE, 3);
		block = new Block(mem);
	}

	@Test
	void constructor() {
		assertEquals(3, block.free());
		assertEquals(3, block.remaining());
		assertNotNull(block.allocations());
		assertEquals(0, block.allocations().count());
	}

	@Test
	void allocate() {
		final DeviceMemory allocation = block.allocate(1);
		assertArrayEquals(new DeviceMemory[]{allocation}, block.allocations().toArray());
		assertEquals(2, block.free());
		assertEquals(2, block.remaining());
	}

	@Test
	void allocateEmpty() {
		block.allocate(3);
		assertThrows(IllegalArgumentException.class, () -> block.allocate(1));
	}

	@Test
	void allocateTooLarge() {
		assertThrows(IllegalArgumentException.class, () -> block.allocate(4));
	}

	@Test
	void allocateDestroyed() {
		mem.destroy();
		assertThrows(IllegalStateException.class, () -> block.allocate(1));
	}

	@Test
	void destroy() {
		final DeviceMemory allocation = block.allocate(1);
		block.destroy();
		assertEquals(true, mem.isDestroyed());
		assertEquals(true, allocation.isDestroyed());
		assertEquals(0, block.allocations().count());
	}

	@Nested
	class BlockDeviceMemoryTests {
		private BlockDeviceMemory allocation;

		@BeforeEach
		void before() {
			allocation = block.allocate(2);
		}

		@Test
		void constructor() {
			assertNotNull(allocation);
			assertEquals(2, allocation.size());
			assertEquals(mem.handle(), allocation.handle());
			assertEquals(Optional.empty(), allocation.region());
			assertEquals(false, allocation.isDestroyed());
		}

		@Test
		void map() {
			final Region region = mem.map(1, 2);
			region.unmap();
			assertEquals(region, allocation.map(1, 2));
		}

		@Test
		void mapDestroyed() {
			allocation.destroy();
			assertThrows(IllegalStateException.class, () -> allocation.map());
		}

		@Test
		void mapReplacePrevious() {
			final Region prev = mem.map();
			assertNotEquals(prev, allocation.map());
		}

		@Test
		void reallocate() {
			allocation.destroy();
			allocation.reallocate(1);
			assertEquals(1, allocation.size());
			assertEquals(false, allocation.isDestroyed());
		}

		@Test
		void reallocateNotDestroyed() {
			assertThrows(IllegalStateException.class, () -> allocation.reallocate(1));
		}

		@Test
		void reallocateBlockDestroyed() {
			mem.destroy();
			assertThrows(IllegalStateException.class, () -> allocation.reallocate(1));
		}

		@Test
		void reallocateTooLarge() {
			allocation.destroy();
			assertThrows(IllegalArgumentException.class, () -> allocation.reallocate(3));
		}

		@Test
		void reallocateUnmapped() {
			allocation.map();
			allocation.destroy();
			allocation.reallocate(1);
			assertEquals(Optional.empty(), allocation.region());
		}

		@Test
		void destroy() {
			allocation.destroy();
			assertEquals(true, allocation.isDestroyed());
			assertEquals(3, block.free());
			assertEquals(1, block.remaining());
		}

		@Test
		void destroyAlreadyDestroyed() {
			allocation.destroy();
			assertThrows(IllegalStateException.class, () -> allocation.destroy());
		}

		@Test
		void isDestroyed() {
			mem.destroy();
			assertEquals(true, allocation.isDestroyed());
		}

		@Test
		void equals() {
			assertEquals(true, allocation.equals(allocation));
			assertEquals(false, allocation.equals(null));
			assertEquals(false, allocation.equals(mock(DeviceMemory.class)));
		}
	}
}
