package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.ReferenceFactory;

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
}
