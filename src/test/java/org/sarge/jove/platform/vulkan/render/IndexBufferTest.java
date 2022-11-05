package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class IndexBufferTest extends AbstractVulkanTest {
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private IndexBuffer index;
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		cmd = mock(Command.Buffer.class);
		buffer = VulkanBufferTest.create(dev, Set.of(VkBufferUsageFlag.INDEX_BUFFER), mock(DeviceMemory.class), SIZE);
		index = new IndexBuffer(buffer, VkIndexType.UINT32);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), index.handle());
		assertEquals(dev, index.device());
		assertEquals(Set.of(VkBufferUsageFlag.INDEX_BUFFER), index.usage());
		assertEquals(SIZE, buffer.length());
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new IndexBuffer(buffer, VkIndexType.NONE_NV));
	}

	@Test
	void length() {
		limit("maxDrawIndexedIndexValue", 0);
		assertThrows(IllegalStateException.class, () -> index.bind(0));
	}

	@Nested
	class IntegerIndex {
		@BeforeEach
		void before() {
			limit("maxDrawIndexedIndexValue", 1);
		}

		@Test
		void type() {
			assertEquals(VkIndexType.UINT32, index.type());
		}

		@Test
		void bind() {
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
			index = new IndexBuffer(buffer, 1);
		}

		@DisplayName("has a short data type")
		@Test
		void type() {
			assertEquals(VkIndexType.UINT16, index.type());
		}

		@DisplayName("is bound with a short data type")
		@Test
		void bind() {
			final Command bind = index.bind(0);
			bind.record(lib, cmd);
			verify(lib).vkCmdBindIndexBuffer(cmd, index, 0, VkIndexType.UINT16);
		}
	}

	@Test
	void equals() {
		assertEquals(true, index.equals(index));
		assertEquals(true, index.equals(new IndexBuffer(buffer, VkIndexType.UINT32)));
		assertEquals(false, index.equals(null));
		assertEquals(false, index.equals(new IndexBuffer(buffer, VkIndexType.UINT16)));
	}
}
