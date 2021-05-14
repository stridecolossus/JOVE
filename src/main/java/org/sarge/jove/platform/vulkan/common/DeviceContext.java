package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

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
}
