package org.sarge.jove.platform.vulkan.core;

import com.sun.jna.Pointer;

// TODO
public class Fence extends AbstractVulkanObject {

	public Fence(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyFence);
	}
}
