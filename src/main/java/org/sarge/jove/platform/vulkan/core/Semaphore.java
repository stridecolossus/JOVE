package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.common.*;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
 * @author Sarge
 */
public interface Semaphore extends NativeObject, TransientObject {
	/**
	 * Creates a new semaphore.
	 * @param device Logical device
	 * @return New semaphore
	 */
	public static Semaphore create(DeviceContext device) {
		final var info = new VkSemaphoreCreateInfo();
		final VulkanLibrary lib = device.library();
		final PointerByReference ref = device.factory().pointer();
		VulkanLibrary.check(lib.vkCreateSemaphore(device, info, null, ref));

		class DefaultSemaphore extends VulkanObject implements Semaphore {
			private DefaultSemaphore() {
				super(new Handle(ref), device);
			}

			@Override
			protected Destructor<DefaultSemaphore> destructor(VulkanLibrary lib) {
				return lib::vkDestroySemaphore;
			}
		}

		return new DefaultSemaphore();
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
