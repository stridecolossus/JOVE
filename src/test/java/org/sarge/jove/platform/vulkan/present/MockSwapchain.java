package org.sarge.jove.platform.vulkan.present;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.MockImage;
import org.sarge.jove.util.Mockery;

public class MockSwapchain extends Swapchain {
	public boolean invalid;

	public MockSwapchain() {
		final var library = new Mockery(Swapchain.Library.class).proxy();
		super(new Handle(2), new MockLogicalDevice(library), List.of(new MockImage()));
	}

	@Override
	public void present(WorkQueue queue, int index, Set<VulkanSemaphore> semaphores) throws Invalidated {
		if(invalid) {
			throw new Invalidated(VkResult.VK_ERROR_DEVICE_LOST);
		}
	}
}
