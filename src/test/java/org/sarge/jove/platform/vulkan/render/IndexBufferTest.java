package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.Mockery;

class IndexBufferTest {
	private IndexBuffer index;
	private VulkanBuffer buffer;
	private Mockery mockery;
	private MockLogicalDevice device;

	@BeforeEach
	void before() {
		// Init device
		mockery = new Mockery(VulkanBuffer.Library.class);
		device = new MockLogicalDevice(mockery.proxy());

		// Create underlying buffer
		buffer = new MockVulkanBuffer(device, 8L, VkBufferUsageFlags.INDEX_BUFFER);

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

	@DisplayName("The Vulkan index type can be determined from the data type of the index")
	@Test
	void type() {
		assertEquals(VkIndexType.UINT8_EXT, IndexBuffer.type(Byte.BYTES));
		assertEquals(VkIndexType.UINT16, IndexBuffer.type(Short.BYTES));
		assertEquals(VkIndexType.UINT32, IndexBuffer.type(Integer.BYTES));
		assertThrows(IllegalArgumentException.class, () -> IndexBuffer.type(Long.BYTES));
	}

	@Test
	void bind() {
		final Command bind = index.bind(0L);
		bind.execute(null);
		assertEquals(1, mockery.mock("vkCmdBindIndexBuffer").count());
	}

	@DisplayName("The length of an index buffer with 32-bit indices must not exceed the hardware limit")
	@Test
	void limit() {
		device.limits.maxDrawIndexedIndexValue = 0;
		assertThrows(IllegalStateException.class, () -> new IndexBuffer(VkIndexType.UINT32, buffer));
	}

	@SuppressWarnings("unused")
	@DisplayName("An index buffer with 8-bit indices requires a device feature")
	@Test
	void bytes() {
		assertThrows(UnsupportedOperationException.class, () -> new IndexBuffer(VkIndexType.UINT8_EXT, buffer));
		device.features.add("indexTypeUint8");
		new IndexBuffer(VkIndexType.UINT8_EXT, buffer);
	}

	@DisplayName("The underlying buffer must be able to be used as an index buffer")
	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), 8L, VkBufferUsageFlags.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new IndexBuffer(VkIndexType.UINT32, invalid));
	}
}
