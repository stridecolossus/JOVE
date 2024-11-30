package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.*;

public class IndexBufferTest {
	private IndexBuffer index;
	private MockDeviceContext dev;
	private Command.CommandBuffer cmd;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		cmd = new MockCommandBuffer();

		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.INDEX_BUFFER)
				.required(VkMemoryProperty.DEVICE_LOCAL)
				.build();

		final var buffer = VulkanBuffer.create(dev, new MockAllocator(), 4, props);
		index = new IndexBuffer(buffer, VkIndexType.UINT32);
	}

	@Test
	void constructor() {
		assertEquals(Set.of(VkBufferUsageFlag.INDEX_BUFFER), index.usage());
		assertEquals(4, index.length());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new IndexBuffer(index, VkIndexType.NONE_NV));
	}

	@Test
	void length() {
		dev.limits().maxDrawIndexedIndexValue = 0;
		assertThrows(IllegalStateException.class, () -> index.bind(0));
	}

	@Nested
	class IntegerIndex {
		@BeforeEach
		void before() {
			dev.limits().maxDrawIndexedIndexValue = 1;
		}

		@Test
		void type() {
			assertEquals(VkIndexType.UINT32, index.type());
		}

		@Test
		void bind() {
			final VulkanLibrary lib = dev.library();
			final Command bind = index.bind(0);
			bind.record(lib, cmd);
			verify(lib).vkCmdBindIndexBuffer(cmd, index, 0, VkIndexType.UINT32);
		}
	}

	@DisplayName("A short index...")
	@Nested
	class ShortIndex {
		@BeforeEach
		void before() {
			index = new IndexBuffer(index, 1);
		}

		@DisplayName("has a short data type")
		@Test
		void type() {
			assertEquals(VkIndexType.UINT16, index.type());
		}

		@DisplayName("is bound with a short data type")
		@Test
		void bind() {
			final VulkanLibrary lib = dev.library();
			final Command bind = index.bind(0);
			bind.record(lib, cmd);
			verify(lib).vkCmdBindIndexBuffer(cmd, index, 0, VkIndexType.UINT16);
		}
	}
}
