package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;

public class MockVulkanSemaphore extends VulkanSemaphore {
	public MockVulkanSemaphore(LogicalDevice device) {
		super(new Handle(1), device);
	}

	public MockVulkanSemaphore() {
		this(new MockLogicalDevice());
	}
}
