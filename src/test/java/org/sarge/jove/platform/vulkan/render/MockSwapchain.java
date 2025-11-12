package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.MockView;

public class MockSwapchain extends Swapchain {
	public MockSwapchain(LogicalDevice device) {
		final var attachment = new MockView(device);
		final var descriptor = attachment.image().descriptor();
		super(new Handle(2), device, device.library(), descriptor.format(), descriptor.extents().size(), List.of(attachment));
	}
}
