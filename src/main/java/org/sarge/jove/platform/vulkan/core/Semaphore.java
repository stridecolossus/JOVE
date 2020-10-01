package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public class Semaphore extends AbstractVulkanObject {
	/**
	 * Creates a semaphore.
	 * @param dev Logical device
	 * @return New semaphore
	 */
	public static Semaphore create(LogicalDevice dev) {
		final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		VulkanLibrary.check(lib.vkCreateSemaphore(dev.handle(), info, null, handle));
		return new Semaphore(handle.getValue(), dev);
	}

	/**
	 * Constructor.
	 * @param handle		Semaphore handle
	 * @param dev			Logical device
	 */
	private Semaphore(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroySemaphore);
	}
}
