package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.Handle;

import com.sun.jna.Pointer;

public class VulkanImage extends Handle {
	private final VkFormat format;
	private final VkExtent2D extent;

	public VulkanImage(Pointer handle, VkFormat format, VkExtent2D extent) {
		super(handle);
		this.format = notNull(format);
		this.extent = notNull(extent);
	}

	public VkFormat format() {
		return format;
	}

	public VkExtent2D extent() {
		return extent;
	}

	@Override
	public synchronized void destroy() {
		// TODO
		super.destroy();
	}
}
