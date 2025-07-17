package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.Pointer;
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
	private VulkanSemaphore(Handle handle, LogicalDevice device) {
		super(handle, device);
	}

	@Override
	protected Destructor<VulkanSemaphore> destructor(VulkanLibrary lib) {
		return lib::vkDestroySemaphore;
	}

	/**
	 * Creates a new semaphore.
	 * @param device Logical device
	 * @return New semaphore
	 */
	public static VulkanSemaphore create(LogicalDevice device) {
		final var info = new VkSemaphoreCreateInfo();
		final VulkanLibrary vulkan = device.vulkan();
		final var ref = new Pointer();
		vulkan.vkCreateSemaphore(device, info, null, ref);
		return new VulkanSemaphore(ref.get(), device);
	}

	/**
	 * Vulkan semaphore API.
	 */
	interface Library {
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
