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
import org.sarge.jove.common.ByteSource.Sink;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class DefaultDeviceMemoryTest extends AbstractVulkanTest {
	private static final int SIZE = 3;

	private DeviceMemory mem;
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
		assertEquals(SIZE, mem.size());
		assertEquals(false, mem.isMapped());
		assertEquals(false, mem.isDestroyed());
	}

	@Nested
	class MappingTests {
		@Test
		void map() {
			final Sink region = mem.map();
			assertNotNull(region);
			assertEquals(true, mem.isMapped());
			verify(lib).vkMapMemory(dev.handle(), mem.handle(), 0, SIZE, 0, POINTER);
		}

		@Test
		void mapSegment() {
			final Sink region = mem.map(2, 1);
			assertNotNull(region);
			assertEquals(true, mem.isMapped());
			verify(lib).vkMapMemory(dev.handle(), mem.handle(), 1, 2, 0, POINTER);
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
	class MappedRegionTests {
		private Sink region;
		private Pointer ptr;

		@BeforeEach
		void before() {
			final PointerByReference ref = mock(PointerByReference.class);
			when(lib.factory().pointer()).thenReturn(ref);

			ptr = mock(Pointer.class);
			when(ref.getValue()).thenReturn(ptr);

			region = mem.map();
		}

		@Test
		void array() {
			region.write(array);
			verify(ptr).write(0, array, 0, array.length);
		}

		@Test
		void arrayNotMapped() {
			mem.unmap();
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
			when(ptr.getByteBuffer(0, SIZE)).thenReturn(dest);

			// Write buffer
			final ByteBuffer src = ByteBuffer.wrap(array);
			region.write(src);
			verify(dest).put(src);
		}

		@Test
		void bufferNotMapped() {
			mem.unmap();
			assertThrows(IllegalStateException.class, () -> region.write(ByteBuffer.wrap(array)));
		}

		@Test
		void bufferInvalidLength() {
			assertThrows(IllegalArgumentException.class, () -> region.write(ByteBuffer.wrap(new byte[4])));
		}

		@Test
		void equals() {
			assertEquals(true, region.equals(region));
			assertEquals(false, region.equals(null));
			assertEquals(false, region.equals(mock(Sink.class)));
		}
	}

	@Nested
	class ReleaseMappingTests {
		@Test
		void unmap() {
			mem.map();
			mem.unmap();
			assertEquals(false, mem.isMapped());
			verify(lib).vkUnmapMemory(dev.handle(), mem.handle());
		}

		@Test
		void unmapNotMapped() {
			assertThrows(IllegalStateException.class, () -> mem.unmap());
		}

		@Test
		void unmapAlreadyUnmapped() {
			mem.map();
			mem.unmap();
			assertThrows(IllegalStateException.class, () -> mem.unmap());
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
