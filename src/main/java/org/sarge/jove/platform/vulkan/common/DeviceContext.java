package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.NativeObject;

/**
 * The <i>device context</i> abstracts the globally used logical device and Vulkan library.
 * @author Sarge
 */
public interface DeviceContext extends NativeObject {
	/**
	 * @param <T> Library type
	 * @return Vulkan library
	 */
	<T> T library();
}
