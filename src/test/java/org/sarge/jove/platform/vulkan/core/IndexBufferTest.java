package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkIndexType;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;
import org.sarge.jove.util.MathsUtil;

public class IndexBufferTest extends AbstractVulkanTest {
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private DeviceMemory mem;
	private IndexBuffer index;
	private Command.Buffer cmd;

	@BeforeEach
	void before() {
		cmd = mock(Command.Buffer.class);
		mem = mock(DeviceMemory.class);
		buffer = new VulkanBuffer(new Handle(1), dev, Set.of(VkBufferUsageFlag.INDEX_BUFFER), mem, SIZE);
		index = new IndexBuffer(buffer, VkIndexType.UINT32);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), index.handle());
		assertEquals(dev, index.device());
		assertEquals(Set.of(VkBufferUsageFlag.INDEX_BUFFER), index.usage());
		assertEquals(mem, buffer.memory());
		assertEquals(SIZE, buffer.length());
		assertEquals(VkIndexType.UINT32, index.type());
	}

	@Test
	void constructorShortIndex() {
		index = new IndexBuffer(buffer);
		assertEquals(VkIndexType.UINT16, index.type());
	}

	@Test
	void constructorShortIndexTooLarge() {
		final long len = 2 * MathsUtil.unsignedMaximum(Short.SIZE);
		buffer = new VulkanBuffer(new Handle(1), dev, Set.of(VkBufferUsageFlag.INDEX_BUFFER), mem, len);
		assertThrows(IllegalArgumentException.class, () -> new IndexBuffer(buffer, VkIndexType.UINT16));
	}

	@Test
	void constructorInvalidType() {
		assertThrows(UnsupportedOperationException.class, () -> new IndexBuffer(buffer, VkIndexType.NONE_NV));
	}

	@Test
	void bind() {
		// Init maximum index length
		property(new VulkanProperty.Key("maxDrawIndexedIndexValue"), 1L, true);

		// Create bind command
		final Command bind = index.bind(0);
		assertNotNull(bind);

		// Bind index
		bind.execute(lib, cmd);
		verify(lib).vkCmdBindIndexBuffer(cmd, index, 0, VkIndexType.UINT32);
	}

	@Test
	void bindShort() {
		index = new IndexBuffer(buffer, VkIndexType.UINT16);
		final Command bind = index.bind(0);
		assertNotNull(bind);
		bind.execute(lib, cmd);
		verify(lib).vkCmdBindIndexBuffer(cmd, index, 0, VkIndexType.UINT16);
	}

	@Test
	void bindInvalidLength() {
		property(new VulkanProperty.Key("maxDrawIndexedIndexValue"), 0L, true);
		assertThrows(IllegalStateException.class, () -> index.bind(0));
	}

	@Test
	void equals() {
		assertEquals(true, index.equals(index));
		assertEquals(true, index.equals(new IndexBuffer(buffer, VkIndexType.UINT32)));
		assertEquals(false, index.equals(null));
		assertEquals(false, index.equals(new IndexBuffer(buffer, VkIndexType.UINT16)));
	}
}
