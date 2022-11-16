package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.AllocationService;
import org.sarge.jove.platform.vulkan.util.DeviceLimits;
import org.sarge.jove.util.ReferenceFactory;

/**
 * The <i>device context</i> abstracts the logical device.
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
