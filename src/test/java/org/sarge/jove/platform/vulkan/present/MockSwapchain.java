package org.sarge.jove.platform.vulkan.present;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.MockView;

public class MockSwapchain extends Swapchain {
	private boolean invalid;

	public MockSwapchain(LogicalDevice device) {
		final var attachment = new MockView(device);
		super(new Handle(2), device, device.library(), List.of(attachment));
	}

	public void invalidate() {
		invalid = true;
	}

	@Override
	public int acquire(VulkanSemaphore semaphore, Fence fence) throws Invalidated {
		if(invalid) {
			throw new Invalidated(VkResult.VK_ERROR_DEVICE_LOST);
		}
		return super.acquire(semaphore, fence);
	}

	@Override
	public void present(WorkQueue queue, int index, Set<VulkanSemaphore> semaphores) throws Invalidated {
		if(invalid) {
			throw new Invalidated(VkResult.VK_ERROR_DEVICE_LOST);
		}
		super.present(queue, index, semaphores);
	}
}
