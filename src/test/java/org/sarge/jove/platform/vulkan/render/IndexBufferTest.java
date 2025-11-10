package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class IndexBufferTest {
	private IndexBuffer index;
	private boolean bound;

	@BeforeEach
	void before() {
		final var library = new MockVulkanLibrary() {
			@Override
			public void vkCmdBindIndexBuffer(Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType) {
				assertEquals(index.buffer(), buffer);
				assertEquals(0L, offset);
				assertEquals(VkIndexType.UINT32, indexType);
				bound = true;
			}
		};
		final var device = new MockLogicalDevice(library);
		final var buffer = new MockVulkanBuffer(device, VkBufferUsageFlag.INDEX_BUFFER);
		index = new IndexBuffer(VkIndexType.UINT32, buffer);
	}

	@Test
	void bind() {
		final Command bind = index.bind(0L);
		bind.execute(null);
		assertEquals(true, bound);
	}

	@Test
	void length() {
		final long length = index.buffer().length();
		assertThrows(IllegalArgumentException.class, () -> index.bind(length));
	}

	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), VkBufferUsageFlag.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new IndexBuffer(VkIndexType.UINT32, invalid));
	}
}
