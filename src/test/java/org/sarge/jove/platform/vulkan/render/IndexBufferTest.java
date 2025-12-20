package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.Mockery;

class IndexBufferTest {
	private IndexBuffer index;
	private Mockery mockery;
	private MockLogicalDevice device;

	@BeforeEach
	void before() {
		// Init device
		mockery = new Mockery(VulkanBuffer.Library.class);
		device = new MockLogicalDevice(mockery.proxy());

		// Create underlying buffer
		final var buffer = new MockVulkanBuffer(device, 8L, VkBufferUsageFlags.INDEX_BUFFER);

		// Init bind command
		@SuppressWarnings("unused")
		final var mock = new Object() {
			public void vkCmdBindIndexBuffer(Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType) {
				assertEquals(0L, offset);
				assertEquals(VkIndexType.UINT32, indexType);
			}

		};
		mockery.implement(mock);

		// Create index buffer
		index = new IndexBuffer(VkIndexType.UINT32, buffer);
	}

	@Test
	void bind() {
		final Command bind = index.bind(0L);
		bind.execute(null);
		assertEquals(1, mockery.mock("vkCmdBindIndexBuffer").count());
	}

	@Test
	void length() {
		assertThrows(IllegalArgumentException.class, () -> index.bind(8L));
	}

	@Test
	void limit() {
		device.limits.maxDrawIndexedIndexValue = 0;
		assertThrows(IllegalStateException.class, () -> index.bind(0L));
	}

	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), 8L, VkBufferUsageFlags.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new IndexBuffer(VkIndexType.UINT32, invalid));
	}
}
