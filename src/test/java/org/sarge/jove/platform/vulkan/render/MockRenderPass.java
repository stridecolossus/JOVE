package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

public class MockRenderPass extends RenderPass {
	public MockRenderPass(LogicalDevice device) {
		super(new Handle(6), device, List.of(Attachment.colour(VkFormat.B8G8R8A8_UNORM)));
	}
}
