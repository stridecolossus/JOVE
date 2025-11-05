package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public class VulkanSemaphore extends VulkanObject {
	/**
	 * Constructor.
	 */
	VulkanSemaphore(Handle handle, LogicalDevice device) {
		super(handle, device);
	}

	@Override
	protected Destructor<VulkanSemaphore> destructor() {
		final Library library = this.device().library();
		return library::vkDestroySemaphore;
	}

	/**
	 * Creates a new semaphore.
	 * @param device Logical device
	 * @return New semaphore
	 */
	public static VulkanSemaphore create(LogicalDevice device) {
		final Library lib = device.library();
		final var ptr = new Pointer();
		lib.vkCreateSemaphore(device, new VkSemaphoreCreateInfo(), null, ptr);
		return new VulkanSemaphore(ptr.get(), device);
	}

	/**
	 * Vulkan semaphore API.
	 */
	public interface Library {
		/**
		 * Creates a semaphore.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pSemaphore		Returned semaphore
		 * @return Result
		 */
		VkResult vkCreateSemaphore(LogicalDevice device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, Pointer pSemaphore);

		/**
		 * Destroys a semaphore.
		 * @param device			Logical device
		 * @param semaphore			Semaphore to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroySemaphore(LogicalDevice device, VulkanSemaphore semaphore, Handle pAllocator);
	}
}
