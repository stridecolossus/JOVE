package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.MappedRegion;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class DefaultDeviceMemoryTest extends AbstractVulkanTest {
	private static final int SIZE = 3;

	private DeviceMemory mem;
	private Pointer handle;
	private PointerByReference ref;
	private byte[] array;

	@BeforeEach
	void before() {
		handle = mock(Pointer.class);
		mem = new DefaultDeviceMemory(handle, dev, SIZE);
		array = new byte[SIZE];
		// TODO...
		ref = mock(PointerByReference.class);
		when(lib.factory().pointer()).thenReturn(ref);
		when(ref.getValue()).thenReturn(mock(Pointer.class));
	}

	@Test
	void constructor() {
		assertEquals(SIZE, mem.size());
		assertEquals(false, mem.isMapped());
		assertEquals(false, mem.isDestroyed());
	}

	@Test
	void map() {
		final MappedRegion region = mem.map();
		assertNotNull(region);
		assertEquals(true, mem.isMapped());
		verify(lib).vkMapMemory(dev.handle(), mem.handle(), 0, SIZE, 0, ref);
	}

	@Test
	void mapSegment() {
		final MappedRegion region = mem.map(2, 1);
		assertNotNull(region);
		assertEquals(true, mem.isMapped());
		verify(lib).vkMapMemory(dev.handle(), mem.handle(), 1, 2, 0, ref);
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

	@Nested
	class MappedRegionTests {
		private MappedRegion region;

		@BeforeEach
		void before() {
			region = mem.map();
		}

		@Test
		void array() {
			region.write(array);
			verify(ref.getValue()).write(0, array, 0, array.length);
		}

		@Test
		void arrayNotMapped() {
			region.unmap();
			assertThrows(IllegalStateException.class, () -> region.write(array));
		}

		@Test
		void arrayInvalidLength() {
			assertThrows(IllegalArgumentException.class, () -> region.write(new byte[4]));
		}

		@Test
		void destroyed() {
			mem.destroy();
			assertThrows(IllegalStateException.class, () -> region.write(array));
		}

		@Test
		void buffer() {
			// Create destination buffer
			final ByteBuffer dest = mock(ByteBuffer.class);
			when(ref.getValue().getByteBuffer(0, SIZE)).thenReturn(dest);

			// Write buffer
			final ByteBuffer src = ByteBuffer.wrap(array);
			region.write(src);
			verify(dest).put(src);
		}

		@Test
		void bufferNotMapped() {
			region.unmap();
			assertThrows(IllegalStateException.class, () -> region.write(ByteBuffer.wrap(array)));
		}

		@Test
		void bufferInvalidLength() {
			assertThrows(IllegalArgumentException.class, () -> region.write(ByteBuffer.wrap(new byte[4])));
		}

		@Test
		void unmap() {
			region.unmap();
			assertEquals(false, mem.isMapped());
			verify(lib).vkUnmapMemory(dev.handle(), mem.handle());
		}

		@Test
		void alreadyUnmapped() {
			region.unmap();
			assertThrows(IllegalStateException.class, () -> region.unmap());
		}

		@Test
		void equals() {
			assertEquals(true, region.equals(region));
			assertEquals(false, region.equals(null));
			assertEquals(false, region.equals(mock(MappedRegion.class)));
		}
	}

	@Test
	void destroy() {
		mem.map();
		mem.destroy();
		assertEquals(true, mem.isDestroyed());
	}

	@Test
	void equals() {
		assertEquals(true, mem.equals(mem));
		assertEquals(true, mem.equals(new DefaultDeviceMemory(handle, dev, SIZE)));
		assertEquals(false, mem.equals(null));
		assertEquals(false, mem.equals(mock(DeviceMemory.class)));
		assertEquals(false, mem.equals(new DefaultDeviceMemory(handle, dev, 42)));
	}
}
