package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceLimits;
import org.sarge.jove.platform.vulkan.core.*;

class ResourceBufferTest {
	private ResourceBuffer resource;
	private LogicalDevice device;

	@BeforeEach
	void before() {
		device = new MockLogicalDevice() {
			@Override
			public DeviceLimits limits() {
				final var limits = new VkPhysicalDeviceLimits();
				limits.maxUniformBufferRange = 42;
				return new DeviceLimits(limits);
			}
		};
		final var buffer = new MockVulkanBuffer(device, VkBufferUsageFlag.UNIFORM_BUFFER);
		resource = new ResourceBuffer(VkDescriptorType.UNIFORM_BUFFER, 0L, buffer);
	}

	@Test
	void build() {
		final VkDescriptorBufferInfo info = resource.descriptor();
		assertEquals(resource.buffer().handle(), info.buffer);
		assertEquals(0L, info.offset);
		assertEquals(42L, info.range);
	}

	@Test
	void length() {
		final var buffer = new MockVulkanBuffer(device, VkBufferUsageFlag.UNIFORM_BUFFER) {
			@Override
			public long length() {
				return 43;
			}
		};
		assertThrows(IllegalStateException.class, () -> new ResourceBuffer(VkDescriptorType.UNIFORM_BUFFER, 0L, buffer));
	}

	@Test
	void invalid() {
		final VulkanBuffer invalid = new MockVulkanBuffer(new MockLogicalDevice(), VkBufferUsageFlag.TRANSFER_SRC);
		assertThrows(IllegalStateException.class, () -> new ResourceBuffer(VkDescriptorType.UNIFORM_BUFFER, 0L, invalid));
	}
}
