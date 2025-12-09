package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.render.FramebufferTest.MockFramebufferLibrary;

public class MockFramebuffer extends Framebuffer {
	public MockFramebuffer() {
		final var device = new MockLogicalDevice(new MockFramebufferLibrary());
		final var group = new Framebuffer.Group(new MockSwapchain(device), new MockRenderPass(device), null);
		super(new Handle(1), device, group);
	}
}
