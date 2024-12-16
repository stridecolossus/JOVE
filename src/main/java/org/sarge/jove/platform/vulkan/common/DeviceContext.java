package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.core.Vulkan;

/**
 * The <i>device context</i> abstracts the logical device.
 * @author Sarge
 */
public interface DeviceContext extends NativeObject {
	/**
	 * @return Vulkan
	 */
	Vulkan vulkan();
}
// TODO - do we really need this?
