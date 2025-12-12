package org.sarge.jove.platform.vulkan.render;

import java.util.List;
import java.util.function.IntFunction;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;

public class MockRenderPass extends RenderPass {
	public MockRenderPass(LogicalDevice device) {
		final IntFunction<View> views = _ -> new MockView(device);
		final var attachment = new Attachment(AttachmentType.COLOUR, AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT), views);
		super(new Handle(6), device, List.of(attachment));
	}
}
