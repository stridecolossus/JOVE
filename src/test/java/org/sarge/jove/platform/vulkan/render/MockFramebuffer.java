package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.render.FramebufferTest.MockFramebufferLibrary;

public class MockFramebuffer extends Framebuffer {
	public MockFramebuffer() {
		final var pass = new MockRenderPass(new MockLogicalDevice(new MockFramebufferLibrary()));
		super(new Handle(1), pass, new Dimensions(640, 480));
	}
}
