package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.util.Mockery;

public class MockFramebuffer extends Framebuffer {
	public MockFramebuffer() {
		final var device = new MockLogicalDevice(new Mockery(Framebuffer.Library.class).proxy());
		super(new Handle(1), device, new MockRenderPass(), new Dimensions(640, 480));
	}
}
