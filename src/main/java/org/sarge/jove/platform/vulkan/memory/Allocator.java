package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.DeviceContext;

import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>memory allocator</i> defines a strategy for allocation of device memory.
 * @see DeviceMemory
 * @author Sarge
 */
@FunctionalInterface
public interface Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
	 */
	class AllocationException extends RuntimeException {
		/**
		 * Constructor.
		 * @param message Message
		 */
		public AllocationException(String message) {
			super(message);
		}

		protected AllocationException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Allocates device memory.
	 * @param type		Type of memory to allocate
	 * @param size		Size of the requested memory (bytes)
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	DeviceMemory allocate(MemoryType type, long size) throws AllocationException;

	/**
	 * Creates a default memory allocator.
	 * @param dev Logical device
	 * @return New default memory allocator
	 */
	static Allocator allocator(DeviceContext dev) {
		return (type, size) -> {
			// Init memory descriptor
			final var info = new VkMemoryAllocateInfo();
			info.allocationSize = oneOrMore(size);
			info.memoryTypeIndex = type.index();

			// Allocate memory
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = lib.factory().pointer();
			check(lib.vkAllocateMemory(dev, info, null, ref));

			// Create memory wrapper
			return new DefaultDeviceMemory(ref.getValue(), dev, size);
		};
	}
}
