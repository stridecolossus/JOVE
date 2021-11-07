package org.sarge.jove.platform.vulkan.memory;

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
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class DefaultDeviceMemoryTest extends AbstractVulkanTest {
	private static final int SIZE = 3;

	private DefaultDeviceMemory mem;
	private Pointer handle;
	private byte[] array;

	@BeforeEach
	void before() {
		handle = mock(Pointer.class);
		mem = new DefaultDeviceMemory(handle, dev, SIZE);
		array = new byte[SIZE];
	}

	@Test
	void constructor() {
		assertEquals(new Handle(handle), mem.handle());
		assertEquals(false, mem.isDestroyed());
		assertEquals(SIZE, mem.size());
		assertEquals(Optional.empty(), mem.region());
	}

	@Test
	void close() {
		mem.map();
		mem.destroy();
		assertEquals(true, mem.isDestroyed());
		assertEquals(Optional.empty(), mem.region());
	}

	@Test
	void equals() {
		assertEquals(true, mem.equals(mem));
		assertEquals(true, mem.equals(new DefaultDeviceMemory(handle, dev, SIZE)));
		assertEquals(false, mem.equals(null));
		assertEquals(false, mem.equals(mock(DeviceMemory.class)));
		assertEquals(false, mem.equals(new DefaultDeviceMemory(handle, dev, 42)));
	}

	@Nested
	class MappingTests {
		@Test
		void map() {
			final Region region = mem.map();
			assertNotNull(region);
			assertEquals(Optional.of(region), mem.region());
			verify(lib).vkMapMemory(dev, mem, 0, SIZE, 0, POINTER);
		}

		@Test
		void mapSegment() {
			final Region region = mem.map(1, 2);
			assertNotNull(region);
			assertEquals(Optional.of(region), mem.region());
			verify(lib).vkMapMemory(dev, mem, 1, 2, 0, POINTER);
		}

		@Test
		void mapAlreadyMapped() {
			mem.map();
			assertThrows(IllegalStateException.class, () -> mem.map());
		}

		@Test
		void mapInvalidSize() {
			assertThrows(IllegalArgumentException.class, () -> mem.map(0, 0));
			assertThrows(IllegalArgumentException.class, () -> mem.map(4, 0));
		}

		@Test
		void mapInvalidOffset() {
			assertThrows(IllegalArgumentException.class, () -> mem.map(SIZE, 1));
		}
	}

	@Nested
	class RegionTests {
		private Region region;
		private Pointer ptr;

		@BeforeEach
		void before() {
			// Create mapped memory pointer
			ptr = mock(Pointer.class);

			// Init reference factory for this pointer
			final PointerByReference ref = mock(PointerByReference.class);
			when(lib.factory().pointer()).thenReturn(ref);
			when(ref.getValue()).thenReturn(ptr);

			// Map memory region
			region = mem.map();
		}

		@Test
		void constructor() {
			assertNotNull(region);
			assertEquals(SIZE, region.size());
			assertEquals(Optional.of(region), mem.region());
		}

		@Test
		void buffer() {
			region.buffer();
			verify(ptr).getByteBuffer(0, SIZE);
		}

		@Test
		void bufferSegment() {
			region.buffer(2, 1);
			verify(ptr).getByteBuffer(2, 1);
		}

		@Test
		void bufferInvalidLength() {
			assertThrows(IllegalArgumentException.class, () -> region.buffer(0, 4));
			assertThrows(IllegalArgumentException.class, () -> region.buffer(1, 3));
		}

		@Test
		void bufferDestroyed() {
			mem.destroy();
			assertThrows(IllegalStateException.class, () -> region.buffer());
		}

		@Test
		void bufferReleased() {
			region.unmap();
			assertThrows(IllegalStateException.class, () -> region.buffer());
		}

		@Test
		void unmap() {
			region.unmap();
			assertEquals(Optional.empty(), mem.region());
			verify(lib).vkUnmapMemory(dev, mem);
		}

		@Test
		void unmapAlreadyReleased() {
			region.unmap();
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@Test
		void unmapDestroyed() {
			mem.destroy();
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@Test
		void equals() {
			assertEquals(true, region.equals(region));
			assertEquals(false, region.equals(null));
			assertEquals(false, region.equals(mock(Region.class)));
		}
	}
}
