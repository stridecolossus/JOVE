package org.sarge.jove.platform.vulkan.render;

import java.util.List;
import java.util.function.IntFunction;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.render.Attachment.AttachmentType;
import org.sarge.jove.util.Mockery;

public class MockRenderPass extends RenderPass {
	public MockRenderPass() {
		final IntFunction<View> views = _ -> new MockView();
		final var attachment = new Attachment(AttachmentType.COLOUR, AttachmentDescription.colour(VkFormat.R32G32B32A32_SFLOAT), views);
		final var device = new MockLogicalDevice(new Mockery(RenderPass.Library.class).proxy());
		super(new Handle(1), device, List.of(attachment));
	}
}
