package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.vulkan.VkBufferUsageFlag.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;

public class VulkanBufferTest {
	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private DeviceContext dev;
	private Allocator allocator;
	private VulkanLibrary lib;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		lib = dev.library();
		mem = new MockDeviceMemory();
		allocator = new MockAllocator();
		buffer = new VulkanBuffer(new Handle(1), dev, Set.of(TRANSFER_SRC, TRANSFER_DST, VERTEX_BUFFER), mem, 2);
	}

	@Test
	void constructor() {
		assertEquals(Set.of(TRANSFER_SRC, TRANSFER_DST, VERTEX_BUFFER), buffer.usage());
		assertEquals(mem, buffer.memory());
		assertEquals(2, buffer.length());
	}

	@Test
	void checkOffset() {
		buffer.checkOffset(0);
		buffer.checkOffset(1);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> buffer.checkOffset(2));
		assertThrows(IllegalArgumentException.class, () -> buffer.checkOffset(-1));
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
	void unsupported() {
		assertThrows(IllegalStateException.class, () -> buffer.require(INDEX_BUFFER));
	}

	@Test
	void create() {
		// Specify buffer properties
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(TRANSFER_SRC)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.build();

		// Create buffer
		buffer = VulkanBuffer.create(dev, allocator, 2, props);
		assertEquals(Set.of(TRANSFER_SRC), buffer.usage());
		assertEquals(2, buffer.length());

		// Check API
		final var expected = new VkBufferCreateInfo() {
			@Override
			public boolean equals(Object obj) {
				final var info = (VkBufferCreateInfo) obj;
				assertEquals(EnumMask.of(TRANSFER_SRC), info.usage);
				assertEquals(VkSharingMode.EXCLUSIVE, info.sharingMode);
				assertEquals(2, info.size);
				return true;
			}
		};
		final var reqs = new VkMemoryRequirements() {
			@Override
			public boolean equals(Object obj) {
				return true;
			}
		};
		verify(lib).vkCreateBuffer(dev, expected, null, dev.factory().pointer());
		verify(lib).vkGetBufferMemoryRequirements(dev, buffer.handle(), reqs);
		verify(lib).vkBindBufferMemory(dev, buffer.handle(), mem, 0L);
	}

	@Test
	void staging() {
		// Create data
		final var data = mock(ByteSizedBufferable.class);
		when(data.length()).thenReturn(2);

		// Create staging buffer
		final var staging = VulkanBuffer.staging(dev, allocator, data);
		assertEquals(Set.of(TRANSFER_SRC), staging.usage());
		assertEquals(2, staging.length());

		// Check data is copied to the buffer
		verify(data).buffer(staging.buffer());
	}

	@DisplayName("A buffer can be destroyed")
	@Test
	void destroy() {
		buffer.destroy();
		verify(lib).vkDestroyBuffer(dev, buffer, null);
		assertEquals(true, mem.isDestroyed());
	}

	@DisplayName("A buffer copy command...")
	@Nested
	class CopyBufferTests {
    	@DisplayName("can be created to copy the whole of the buffer")
    	@Test
    	void copy() {
    		final VulkanBuffer dest = new VulkanBuffer(new Handle(2), dev, Set.of(VkBufferUsageFlag.TRANSFER_DST), mem, 2);
    		final BufferCopyCommand copy = buffer.copy(dest);
    		copy.execute(lib, new MockCommandBuffer());
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
    		final var cmd = mock(Command.CommandBuffer.class);
    		fill.execute(lib, cmd);
    		verify(lib).vkCmdFillBuffer(cmd, buffer, 0, VulkanBuffer.VK_WHOLE_SIZE, 42);
    	}

    	@DisplayName("must have a valid buffer offset")
    	@Test
    	void offset() {
    		assertThrows(IllegalArgumentException.class, () -> buffer.fill(2, VulkanBuffer.VK_WHOLE_SIZE, 42));
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
    		final VulkanBuffer invalid = new VulkanBuffer(new Handle(2), dev, Set.of(VkBufferUsageFlag.TRANSFER_SRC), mem, 2);
    		assertThrows(IllegalStateException.class, () -> invalid.fill(0, VulkanBuffer.VK_WHOLE_SIZE, 42));
    	}
    }
}
