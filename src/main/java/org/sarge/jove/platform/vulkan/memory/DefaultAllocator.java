package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>default allocator</i> allocates new device memory instances on demand.
 * @author Sarge
 */
public class DefaultAllocator implements Allocator {
	private final DeviceContext dev;

	/**
	 * Constructor.
	 * @param dev Logical device
	 */
	public DefaultAllocator(DeviceContext dev) {
		this.dev = notNull(dev);
	}

	@Override
	public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		// Init memory descriptor
		final var info = new VkMemoryAllocateInfo();
		info.allocationSize = oneOrMore(size);
		info.memoryTypeIndex = type.index();

		// Allocate memory
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkAllocateMemory(dev, info, null, ref));

		// Create memory wrapper
		final DefaultDeviceMemory mem = new DefaultDeviceMemory(new Handle(ref), dev, size);
		if(type.isHostVisible()) {
			return mem;
		}
		else {
			return new DefaultDeviceMemory(mem) {
				@Override
				public boolean isHostVisible() {
					return false;
				}
			};
		}
	}
}
