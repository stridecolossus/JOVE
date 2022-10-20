package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.util.DeviceLimits;

/**
 * The <i>device context</i> represents an abstraction for objects dependant on the logical device.
 * @author Sarge
 */
public interface DeviceContext {
	/**
	 * @return Vulkan API
	 */
	VulkanLibrary library();

	/**
	 * @return Logical device handle
	 */
	Handle handle();

	/**
	 * @return Reference factory
	 */
	ReferenceFactory factory();

	/**
	 * @return Memory allocation service
	 */
	AllocationService allocator();

	/**
	 * @return Hardware limits for this device
	 */
	DeviceLimits limits();
}
