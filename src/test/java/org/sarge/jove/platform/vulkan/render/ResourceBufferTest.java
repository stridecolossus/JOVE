package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ResourceBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.UNIFORM_BUFFER);

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
		when(buffer.length()).thenReturn(4L);

		// Init limit
		limit("maxUniformBufferRange", 4);

		// Create resource buffer
		res = new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 0);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), res.handle());
		assertEquals(dev, res.device());
		assertEquals(FLAGS, res.usage());
		assertEquals(buffer.memory(), res.memory());
		assertEquals(4L, res.length());
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
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 4));
	}

	@Test
	void invalidBufferLength() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 5));
	}

	@Test
	void populate() {
		final var write = new VkWriteDescriptorSet();
		res.populate(write);

		final VkDescriptorBufferInfo info = write.pBufferInfo;
		assertNotNull(info);
		assertEquals(res.handle(), info.buffer);
		assertEquals(res.length(), info.range);
		assertEquals(0, info.offset);
	}

	@Test
	void offsetInvalid() {
		assertThrows(IllegalArgumentException.class, () -> res.offset(4L));
	}

	@Test
	void equals() {
		assertEquals(true, res.equals(res));
		assertEquals(true, res.equals(new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 0)));
		assertEquals(false, res.equals(null));
		assertEquals(false, res.equals(new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 1)));
	}
}
