package org.sarge.jove.platform.vulkan.memory;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.HostVisible;

/**
 * Implementation for memory that can be mapped by the application.
 * @author Sarge
 */
class HostVisibleDeviceMemory extends DefaultDeviceMemory implements HostVisible {
	HostVisibleDeviceMemory(Handle handle, DeviceContext dev, long size) {
		super(handle, dev, size);
	}
}
