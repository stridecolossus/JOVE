package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class IndexBufferTest {
	private IndexBuffer index;
	private int max;
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

		final var device = new MockLogicalDevice(library) {
			@Override
			public DeviceLimits limits() {
				final var limits = new VkPhysicalDeviceLimits();
				limits.maxDrawIndexedIndexValue = max;
				return new DeviceLimits(limits);
			}
		};
		max = 2;

		final var buffer = new MockVulkanBuffer(device, 8L, VkBufferUsageFlags.INDEX_BUFFER);
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
		assertThrows(IllegalArgumentException.class, () -> index.bind(8L));
	}

	@Test
	void limit() {
		max = 1;
		assertThrows(IllegalStateException.class, () -> index.bind(0L));
	}

	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), 8L, VkBufferUsageFlags.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new IndexBuffer(VkIndexType.UINT32, invalid));
	}
}
