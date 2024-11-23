package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public class VulkanSemaphore extends VulkanObject {
	/**
	 * Constructor.
	 */
	private VulkanSemaphore(Handle handle, DeviceContext device) {
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
	public static VulkanSemaphore create(DeviceContext device) {
		final var info = new VkSemaphoreCreateInfo();
		final Vulkan vulkan = device.vulkan();
		final var lib = vulkan.library();
		final PointerReference ref = vulkan.factory().pointer();
		lib.vkCreateSemaphore(device, info, null, ref);
		return new VulkanSemaphore(ref.handle(), device);
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
		int vkCreateSemaphore(DeviceContext device, VkSemaphoreCreateInfo pCreateInfo, Handle pAllocator, PointerReference pSemaphore);

		/**
		 * Destroys a semaphore.
		 * @param device			Logical device
		 * @param semaphore			Semaphore to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroySemaphore(DeviceContext device, VulkanSemaphore semaphore, Handle pAllocator);
	}
}
