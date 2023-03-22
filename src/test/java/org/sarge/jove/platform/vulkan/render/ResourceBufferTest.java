package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkDescriptorType.UNIFORM_BUFFER;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.memory.*;

public class ResourceBufferTest {
	private static final Set<VkBufferUsageFlag> FLAGS = Set.of(VkBufferUsageFlag.UNIFORM_BUFFER);

	private VulkanBuffer buffer;
	private ResourceBuffer res;

	@BeforeEach
	void before() {
		// Init device
		final var dev = new MockDeviceContext();
		dev.limits().maxUniformBufferRange = 4;

		// Configure resource buffer
		final var props = new MemoryProperties.Builder<VkBufferUsageFlag>()
				.usage(VkBufferUsageFlag.UNIFORM_BUFFER)
				.build();

		// Create resource buffer
		buffer = VulkanBuffer.create(dev, new MockAllocator(), 4, props);
		res = new ResourceBuffer(buffer, UNIFORM_BUFFER, 0);
	}

	@Test
	void constructor() {
		assertEquals(FLAGS, res.usage());
		assertEquals(4L, res.length());
		assertEquals(UNIFORM_BUFFER, res.type());
	}

	@DisplayName("A resource buffer must have a valid descriptor type")
	@Test
	void type() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.SAMPLER, 0));
	}

	@DisplayName("The type of a resource buffer must be supported by the underlying buffer")
	@Test
	void unsupported() {
		assertThrows(IllegalStateException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.STORAGE_BUFFER, 0));
	}

	@DisplayName("The offset of a resource buffer cannot exceed the underlying buffer")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new ResourceBuffer(buffer, VkDescriptorType.UNIFORM_BUFFER, 4));
	}

	@Test
	void build() {
		final VkDescriptorBufferInfo info = res.build();
		assertEquals(res.handle(), info.buffer);
		assertEquals(res.length(), info.range);
		assertEquals(0, info.offset);
	}

	@Test
	void offset() {
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
