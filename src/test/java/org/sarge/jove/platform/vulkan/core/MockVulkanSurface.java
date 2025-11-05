package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanSurfaceTest.MockVulkanSurfaceLibrary;

public class MockVulkanSurface extends VulkanSurface {
	public MockVulkanSurface() {
		super(new Handle(3), new MockPhysicalDevice(), new MockVulkanSurfaceLibrary());
	}
}
