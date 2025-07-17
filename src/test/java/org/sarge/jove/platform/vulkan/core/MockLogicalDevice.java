package org.sarge.jove.platform.vulkan.core;

import java.util.Map;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;

public class MockLogicalDevice extends LogicalDevice {
	public MockLogicalDevice() {
		this(new MockVulkanLibrary());
	}

	public MockLogicalDevice(VulkanLibrary lib) {
		super(new Handle(1), lib, null, new VkPhysicalDeviceLimits(), Map.of());
	}
}
