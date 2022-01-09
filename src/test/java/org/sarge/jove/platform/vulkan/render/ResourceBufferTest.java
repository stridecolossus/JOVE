package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkDescriptorBufferInfo;
import org.sarge.jove.platform.vulkan.VkDescriptorType;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ResourceBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.UNIFORM_BUFFER);
	private static final long SIZE = 4;

	private VulkanBuffer buffer;
	private ResourceBuffer res;

	@BeforeEach
	void before() {
		// Create underlying buffer
		buffer = mock(VulkanBuffer.class);
		when(buffer.handle()).thenReturn(new Handle(1));
		when(buffer.device()).thenReturn(dev);
		when(buffer.memory()).thenReturn(mock(DeviceMemory.class));
		when(buffer.usage()).thenReturn(FLAGS);
		when(buffer.length()).thenReturn(SIZE);

		// Create resource buffer
		res = new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 0);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), res.handle());
		assertEquals(dev, res.device());
		assertEquals(FLAGS, res.usage());
		assertEquals(buffer.memory(), res.memory());
		assertEquals(SIZE, res.length());
		assertEquals(VkDescriptorType.UNIFORM_BUFFER, res.type());
	}

	@Test
	void invalidDescriptorType() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.SAMPLER, 0));
	}

	@Test
	void unsupportedBufferUsage() {
		assertThrows(IllegalStateException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.STORAGE_BUFFER, 0));
	}

	@Test
	void invalidBufferOffset() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, SIZE));
	}

	@Test
	void populate() {
		final VkDescriptorBufferInfo info = res.populate();
		assertNotNull(info);
		assertEquals(res.handle(), info.buffer);
		assertEquals(res.length(), info.range);
		assertEquals(0, info.offset);
	}

	@Test
	void offset() {
		final ResourceBuffer offset = res.offset(1);
		assertNotNull(offset);
		assertEquals(1, offset.populate().offset);
	}

	@Test
	void offsetInvalid() {
		assertThrows(IllegalArgumentException.class, () -> res.offset(SIZE));
	}

	@Test
	void equals() {
		assertEquals(true, res.equals(res));
		assertEquals(true, res.equals(new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 0)));
		assertEquals(false, res.equals(null));
		assertEquals(false, res.equals(new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 1)));
	}
}
