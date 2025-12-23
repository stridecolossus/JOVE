package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.present.MockSwapchain;
import org.sarge.jove.util.Mockery;

public class MockRenderPass extends RenderPass {
	public MockRenderPass() {
		final var device = new MockLogicalDevice(new Mockery(RenderPass.Library.class).proxy());

		final var attachment = new ColourAttachment(AttachmentDescription.colour(), () -> new MockSwapchain()) {
			@Override
			protected List<View> views(LogicalDevice device, Dimensions extents) {
				return List.of(new MockView());
			}
		};

		super(new Handle(1), device, List.of(attachment));
	}
}
