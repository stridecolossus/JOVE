package org.sarge.jove.platform.vulkan.present;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.util.Mockery;

public class MockSwapchain extends Swapchain {
	public boolean invalid;

	public MockSwapchain() {
		final var library = new Mockery(Swapchain.Library.class).proxy();
		super(new Handle(2), new MockLogicalDevice(library), VkFormat.B8G8R8_UNORM, new Dimensions(640, 480));
	}

	@Override
	public List<Image> attachments() {
		return List.of(new MockImage());
	}

	@Override
	public void present(WorkQueue queue, int index, Set<VulkanSemaphore> semaphores) throws Invalidated {
		if(invalid) {
			throw new Invalidated(VkResult.VK_ERROR_DEVICE_LOST);
		}
	}
}
