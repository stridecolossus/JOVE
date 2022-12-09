package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.sarge.jove.platform.vulkan.VkDescriptorType.UNIFORM_BUFFER;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ResourceBufferTest extends AbstractVulkanTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.UNIFORM_BUFFER);

	private VulkanBuffer buffer;
	private ResourceBuffer res;

	@BeforeEach
	void before() {
		limit("maxUniformBufferRange", 4);
		buffer = VulkanBufferTest.create(dev, FLAGS, mock(DeviceMemory.class),  4);
		res = new ResourceBuffer(buffer, UNIFORM_BUFFER, 0);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(1), res.handle());
		assertEquals(dev, res.device());
		assertEquals(FLAGS, res.usage());
		assertEquals(4L, res.length());
		assertEquals(UNIFORM_BUFFER, res.type());
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
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, UNIFORM_BUFFER, 4));
	}

	@Test
	void invalidBufferLength() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, UNIFORM_BUFFER, 5));
	}

	@Test
	void build() {
		final VkDescriptorBufferInfo info = res.build();
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
		assertEquals(true, res.equals(new ResourceBuffer(buffer, UNIFORM_BUFFER, 0)));
		assertEquals(false, res.equals(null));
		assertEquals(false, res.equals(new ResourceBuffer(buffer, UNIFORM_BUFFER, 1)));
	}
}
