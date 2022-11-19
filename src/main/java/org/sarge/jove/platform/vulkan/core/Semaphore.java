package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public class Semaphore extends AbstractVulkanObject {
	/**
	 * Creates a new semaphore.
	 * @param dev Logical device
	 * @return New semaphore
	 */
	public static Semaphore create(DeviceContext dev) {
		final var info = new VkSemaphoreCreateInfo();
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		VulkanLibrary.check(lib.vkCreateSemaphore(dev, info, null, ref));
		return new Semaphore(new Handle(ref), dev);
	}

	private Semaphore(Handle handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Semaphore> destructor(VulkanLibrary lib) {
		return lib::vkDestroySemaphore;
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
		int vkCreateSemaphore(DeviceContext device, VkSemaphoreCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSemaphore);

		/**
		 * Destroys a semaphore.
		 * @param device			Logical device
		 * @param semaphore			Semaphore to destroy
		 * @param pAllocator		Allocator
		 */
		void vkDestroySemaphore(DeviceContext device, Semaphore semaphore, Pointer pAllocator);
	}
}
