package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlags;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.Mockery;

class VertexBufferTest {
	private VertexBuffer vertex;
	private Mockery mockery;

	@BeforeEach
	void before() {
		// Init buffer library
		@SuppressWarnings("unused")
		final var mock = new Object() {
			public void vkCmdBindVertexBuffers(Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets) {
				assertEquals(0, firstBinding);
				assertEquals(1, bindingCount);
				assertArrayEquals(new VulkanBuffer[]{vertex.buffer()}, pBuffers);
				assertArrayEquals(new long[]{0L}, pOffsets);
			}
		};
		mockery = new Mockery(VulkanBuffer.Library.class);
		mockery.implement(mock);

		// Create VBO
		final var device = new MockLogicalDevice(mockery.proxy());
		final var buffer = new MockVulkanBuffer(device, 42, VkBufferUsageFlags.VERTEX_BUFFER);
		vertex = new VertexBuffer(buffer);
	}

	@Test
	void bind() {
		final var mock = mockery.mock("vkCmdBindVertexBuffers");
		final Command bind = vertex.bind(0);
		bind.execute(null);
		assertEquals(1, mock.count());
	}

	@Test
	void invalid() {
		final var device = new MockLogicalDevice(mockery.proxy());
		final var buffer = new MockVulkanBuffer(device, 42, VkBufferUsageFlags.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new VertexBuffer(buffer));
	}
}
