package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.platform.desktop.MockWindow;
import org.sarge.jove.platform.vulkan.core.VulkanSurfaceTest.MockVulkanSurfaceLibrary;

public class MockVulkanSurface extends VulkanSurface {
	public MockVulkanSurface() {
		this(new MockVulkanSurfaceLibrary());
	}

	public MockVulkanSurface(Library library) {
		super(new MockWindow(), new MockInstance(), library);
	}
}
