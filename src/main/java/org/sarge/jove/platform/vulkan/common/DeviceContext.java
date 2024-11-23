package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.Vulkan;

/**
 * The <i>device context</i> abstracts the logical device.
 * @author Sarge
 */
public interface DeviceContext {
	/**
	 * @return Vulkan
	 */
	Vulkan vulkan();

	/**
	 * @return Logical device handle
	 */
	Handle handle();

//	/**
//	 * @return Reference factory
//	 */
//	ReferenceFactory factory();
//
//	/**
//	 * @return Features enabled on this device
//	 */
//	DeviceFeatures features();
//
//	/**
//	 * @return Hardware limits for this device
//	 */
//	VkPhysicalDeviceLimits limits();
//
//	/**
//	 * Waits for this device to become idle.
//	 */
//	void waitIdle();
}
// TODO - remove
