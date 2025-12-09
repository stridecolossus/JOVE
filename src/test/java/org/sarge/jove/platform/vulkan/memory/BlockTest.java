package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.memory.Block.BlockDeviceMemory;

public class BlockTest {
	private Block block;
	private DeviceMemory memory;
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		final var device = new MockLogicalDevice(new MockMemoryLibrary());
		allocator = Arena.ofAuto();
		memory = new DefaultDeviceMemory(new Handle(allocator.allocate(3)), device, MockAllocator.TYPE, 3);
		block = new Block(memory);
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
		memory.destroy();
		assertThrows(IllegalStateException.class, () -> block.allocate(1));
	}

	@Test
	void destroy() {
		final DeviceMemory allocation = block.allocate(1);
		block.destroy();
		assertEquals(true, memory.isDestroyed());
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
			assertEquals(memory.handle(), allocation.handle());
			assertEquals(Optional.empty(), allocation.region());
			assertEquals(false, allocation.isDestroyed());
		}

		@Test
		void map() {
			final Region region = memory.map(1, 2);
			assertEquals(2, region.size());
		}

		@Test
		void mapDestroyed() {
			allocation.destroy();
			assertThrows(IllegalStateException.class, () -> allocation.map(0, 2));
		}

		@Test
		void mapReplacePrevious() {
			memory.map(0, 1);
			allocation.map(0, 1);
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
			memory.destroy();
			assertThrows(IllegalStateException.class, () -> allocation.reallocate(1));
		}

		@Test
		void reallocateTooLarge() {
			allocation.destroy();
			assertThrows(IllegalArgumentException.class, () -> allocation.reallocate(3));
		}

		@Test
		void reallocateUnmapped() {
			allocation.map(0, 1);
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
			memory.destroy();
			assertEquals(true, allocation.isDestroyed());
		}

		@Test
		void equals() {
			assertEquals(true, allocation.equals(allocation));
			assertEquals(false, allocation.equals(null));
		}
	}
}
