package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public class Semaphore extends AbstractVulkanObject {
	/**
	 * Creates a new semaphore.
	 * @return New semaphore
	 */
	public static Semaphore create(DeviceContext dev) {
		final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = dev.factory().pointer();
		VulkanLibrary.check(lib.vkCreateSemaphore(dev, info, null, handle));
		return new Semaphore(handle.getValue(), dev);
	}

	private Semaphore(Pointer handle, DeviceContext dev) {
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
		 * @param device			Device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pSemaphore		Returned semaphore
		 * @return Result code
		 */
		int vkCreateSemaphore(DeviceContext device, VkSemaphoreCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSemaphore);

		/**
		 * Destroys a semaphore.
		 * @param device			Device
		 * @param semaphore			Semaphore
		 * @param pAllocator		Allocator
		 */
		void vkDestroySemaphore(DeviceContext device, Semaphore semaphore, Pointer pAllocator);
	}
}
