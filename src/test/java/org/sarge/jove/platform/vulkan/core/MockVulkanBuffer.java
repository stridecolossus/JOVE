package org.sarge.jove.platform.vulkan.core;

import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.memory.MockDeviceMemory;

public class MockVulkanBuffer extends VulkanBuffer {
	public MockVulkanBuffer(LogicalDevice device, VkBufferUsageFlag... usage) {
		super(new Handle(1), device, Set.of(usage), new MockDeviceMemory(), 42L);
	}
}
