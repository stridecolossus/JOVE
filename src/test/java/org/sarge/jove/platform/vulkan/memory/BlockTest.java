package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.memory.Block.BlockDeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;

public class BlockTest {
	private Block block;
	private DeviceMemory mem;

	@BeforeEach
	void before() {
		// Create parent memory
		mem = mock(DeviceMemory.class);
		when(mem.size()).thenReturn(3L);
		when(mem.handle()).thenReturn(new Handle(4));

		// Create block
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
	void destroy() {
		final DeviceMemory allocation = block.allocate(1);
		block.destroy();
		verify(mem).close();
		when(mem.isDestroyed()).thenReturn(true);
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
			allocation.map(1, 2);
			verify(mem).map(1, 2);
		}

		@Test
		void mapDestroyed() {
			allocation.close();
			assertThrows(IllegalStateException.class, () -> allocation.map());
		}

		@Test
		void mapReplacePrevious() {
			// Map region
			allocation.map();

			// Mock previous mapping
			final Region prev = mock(Region.class);
			when(mem.region()).thenReturn(Optional.of(prev));

			// Map again and check previous mapping is released
			allocation.map();
			verify(prev).unmap();
		}

		@Test
		void reallocate() {
			allocation.close();
			allocation.reallocate();
			assertEquals(false, allocation.isDestroyed());
		}

		@Test
		void reallocateNotDestroyed() {
			assertThrows(IllegalStateException.class, () -> allocation.reallocate());
		}

		@Test
		void reallocateBlockDestroyed() {
			mem.close();
			assertThrows(IllegalStateException.class, () -> allocation.reallocate());
		}

		@Test
		void close() {
			allocation.close();
			assertEquals(true, allocation.isDestroyed());
			assertEquals(3, block.free());
			assertEquals(1, block.remaining());
		}

		@Test
		void closeAlreadyDestroyed() {
			allocation.close();
			assertThrows(IllegalStateException.class, () -> allocation.close());
		}

		@Test
		void isDestroyed() {
			when(mem.isDestroyed()).thenReturn(true);
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