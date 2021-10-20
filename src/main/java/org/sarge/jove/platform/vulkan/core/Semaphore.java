package org.sarge.jove.platform.vulkan.core;

import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
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
		final PointerByReference handle = lib.factory().pointer();
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
}
