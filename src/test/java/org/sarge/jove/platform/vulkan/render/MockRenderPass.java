package org.sarge.jove.platform.vulkan.render;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

public class MockRenderPass extends RenderPass {
	public MockRenderPass(LogicalDevice device) {
		super(new Handle(6), device, device.library());
	}
}
