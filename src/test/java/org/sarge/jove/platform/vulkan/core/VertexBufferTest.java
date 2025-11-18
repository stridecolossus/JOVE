package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class VertexBufferTest {
	private VertexBuffer vertex;
	private boolean bound;

	@BeforeEach
	void before() {
		final var library = new MockVulkanLibrary() {
			@Override
			public void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets) {
				assertEquals(0, firstBinding);
				assertEquals(1, bindingCount);
				assertArrayEquals(new VulkanBuffer[]{vertex.buffer()}, pBuffers);
				assertArrayEquals(new long[]{0L}, pOffsets);
				bound = true;
			}
		};
		final var device = new MockLogicalDevice(library);
		final VulkanBuffer buffer = new MockVulkanBuffer(device, 42, VkBufferUsageFlag.VERTEX_BUFFER);
		vertex = new VertexBuffer(buffer);
	}

	@Test
	void bind() {
		final Command bind = vertex.bind(0);
		bind.execute(null);
		assertEquals(true, bound);
	}

	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), 42, VkBufferUsageFlag.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new VertexBuffer(invalid));
	}
}
