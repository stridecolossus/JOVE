package org.sarge.jove.platform.vulkan.present;

import org.sarge.jove.platform.desktop.MockWindow;
import org.sarge.jove.platform.vulkan.core.MockInstance;
import org.sarge.jove.platform.vulkan.present.VulkanSurface;
import org.sarge.jove.platform.vulkan.present.VulkanSurfaceTest.MockVulkanSurfaceLibrary;

public class MockVulkanSurface extends VulkanSurface {
	public MockVulkanSurface() {
		this(new MockVulkanSurfaceLibrary());
	}

	public MockVulkanSurface(Library library) {
		super(new MockWindow(), new MockInstance(), library);
	}
}
