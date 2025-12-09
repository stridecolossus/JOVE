package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.MockView;

public class MockSwapchain extends Swapchain {
	private boolean invalid;

	public MockSwapchain(LogicalDevice device) {
		final var attachment = new MockView(device);
		final var descriptor = attachment.image().descriptor();
		super(new Handle(2), device, device.library(), descriptor.format(), descriptor.extents().size(), List.of(attachment));
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
	public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws Invalidated {
		if(invalid) {
			throw new Invalidated(VkResult.VK_ERROR_DEVICE_LOST);
		}
		super.present(queue, index, semaphore);
	}
}
