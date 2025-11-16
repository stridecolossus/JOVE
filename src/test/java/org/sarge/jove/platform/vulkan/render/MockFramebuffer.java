package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.image.MockView;

public class MockFramebuffer extends Framebuffer {
	public MockFramebuffer() {
		final var device = new MockLogicalDevice();
		final var extents = new Rectangle(0, 0, 1024, 768);
		final var view = new MockView(device);
		super(new Handle(1), device, new Handle(2), List.of(view), extents, device.library());
	}
}
