package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlag.*;

import java.nio.ByteBuffer;
import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class VulkanBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(TRANSFER_SRC, TRANSFER_DST, VERTEX_BUFFER);
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private Region region;
	private ByteBuffer bb;

	@BeforeEach
	void before() {
		// Init device memory
		mem = mock(DeviceMemory.class);
		when(mem.handle()).thenReturn(new Handle(new Pointer(1)));
		when(mem.size()).thenReturn(SIZE);

		// Init mapped region
		bb = mock(ByteBuffer.class);
		region = mock(Region.class);
		when(mem.map()).thenReturn(region);
		when(region.buffer()).thenReturn(bb);

		// Init memory allocator
		when(allocator.allocate(isA(VkMemoryRequirements.class), isA(MemoryProperties.class))).thenReturn(mem);

		// Create buffer
		buffer = create(dev, FLAGS, mem, SIZE);
	}

	public static VulkanBuffer create(DeviceContext dev, Set<VkBufferUsageFlag> usage, DeviceMemory mem, long size) {
		return new VulkanBuffer(new Handle(1), dev, usage, mem, size);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), buffer.handle());
		assertEquals(dev, buffer.device());
		assertEquals(FLAGS, buffer.usage());
		assertEquals(mem, buffer.memory());
		assertEquals(SIZE, buffer.length());
	}

	@Test
	void copyConstructor() {
		final VulkanBuffer copy = new VulkanBuffer(buffer);
		assertEquals(buffer, copy);
	}

	@Test
	void validate() {
		buffer.validate(0);
		buffer.validate(2);
	}

	@Test
	void validateInvalidOffset() {
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(SIZE));
		assertThrows(IllegalArgumentException.class, () -> buffer.validate(-1));
	}

	@Test
	void require() {
		buffer.require();
		buffer.require(TRANSFER_SRC);
		buffer.require(TRANSFER_DST);
		buffer.require(VERTEX_BUFFER);
		buffer.require(TRANSFER_SRC, TRANSFER_DST, VERTEX_BUFFER);
	}

	@Test
	void requireNotSupported() {
		assertThrows(IllegalStateException.class, () -> buffer.require(INDEX_BUFFER));
		assertThrows(IllegalStateException.class, () -> buffer.require(TRANSFER_SRC, INDEX_BUFFER));
	}

	@Test
	void buffer() {
		assertEquals(bb, buffer.buffer());
	}

	@Test
	void create() {
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();
		buffer = VulkanBuffer.create(dev, SIZE, props);
		assertEquals(Set.of(TRANSFER_SRC), buffer.usage());
	}

	@Test
	void staging() {
		// Create data
		final var data = mock(ByteSizedBufferable.class);
		when(data.length()).thenReturn((int) SIZE);

		// Create staging buffer
		final VulkanBuffer staging = VulkanBuffer.staging(dev, data);
		assertNotNull(staging);
		assertEquals(Set.of(TRANSFER_SRC), staging.usage());
		assertEquals(SIZE, staging.length());

		// Check data is copied to buffer
		verify(region).buffer();
		verify(data).buffer(bb);
	}

	@DisplayName("A buffer can be destroyed")
	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyBuffer(dev, buffer, null);
		verify(mem).destroy();
	}

	@DisplayName("A buffer copy command...")
	@Nested
	class CopyBufferTests {
    	@DisplayName("can be created to copy the whole of the buffer")
    	@Test
    	void copy() {
    		final VulkanBuffer dest = create(dev, Set.of(VkBufferUsageFlag.TRANSFER_DST), mem, SIZE);
    		final BufferCopyCommand copy = buffer.copy(dest);
    		copy.record(lib, mock(Command.Buffer.class));
    	}

    	@DisplayName("cannot copy a buffer to itself")
    	@Test
    	void self() {
    		assertThrows(IllegalArgumentException.class, () -> buffer.copy(buffer));
    	}
	}

	@DisplayName("A buffer fill command...")
	@Nested
	class FillBufferTests {
    	@Test
    	void fill() {
    		final Command fill = buffer.fill(0, VulkanBuffer.VK_WHOLE_SIZE, 42);
    		final var cmd = mock(Command.Buffer.class);
    		fill.record(lib, cmd);
    		verify(lib).vkCmdFillBuffer(cmd, buffer, 0, VulkanBuffer.VK_WHOLE_SIZE, 42);
    	}

    	@DisplayName("must have a valid buffer offset")
    	@Test
    	void offset() {
    		assertThrows(IllegalArgumentException.class, () -> buffer.fill(SIZE, VulkanBuffer.VK_WHOLE_SIZE, 42));
    	}

    	@DisplayName("must have an offset and size with the correct alignment")
    	@Test
    	void alignment() {
    		assertThrows(IllegalArgumentException.class, () -> buffer.fill(3, VulkanBuffer.VK_WHOLE_SIZE, 42));
    		assertThrows(IllegalArgumentException.class, () -> buffer.fill(0, 3, 42));
    	}

    	@DisplayName("must be created from a buffer that is a transfer destination")
    	@Test
    	void destination() {
    		final VulkanBuffer invalid = create(dev, Set.of(VkBufferUsageFlag.TRANSFER_SRC), mem, SIZE);
    		assertThrows(IllegalStateException.class, () -> invalid.fill(0, VulkanBuffer.VK_WHOLE_SIZE, 42));
    	}
    }
}
