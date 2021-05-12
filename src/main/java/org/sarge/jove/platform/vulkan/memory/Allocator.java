package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>memory allocator</i> defines a strategy for allocation of device memory.
 * @see DeviceMemory
 * @author Sarge
 */
public interface Allocator {
	/**
	 * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
	 */
	class AllocationException extends RuntimeException {
		protected AllocationException(String message) {
			super(message);
		}

		protected AllocationException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Allocates device memory.
	 * @param type Type of memory to allocate
	 * @param size Size of the memory (bytes)
	 * @return New device memory
	 * @throws AllocationException if the memory cannot be allocated
	 */
	DeviceMemory allocate(MemoryType type, long size) throws AllocationException;

	/**
	 * Default implementation that allocates a new region of memory on <b>every</b> invocation.
	 */
	class SimpleAllocator implements Allocator {
		private final LogicalDevice dev;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public SimpleAllocator(LogicalDevice dev) {
			this.dev = notNull(dev);
			// TODO - cyclic with core, factor out device handle & library maybe? or revert to singletons for lib/factory?
		}

		@Override
		public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
			// Init memory descriptor
			final var info = new VkMemoryAllocateInfo();
			info.allocationSize = oneOrMore(size);
			info.memoryTypeIndex = type.index();

			// Allocate memory
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = lib.factory().pointer();
			check(dev.library().vkAllocateMemory(dev.handle(), info, null, ref));

			// Create memory wrapper
			return new DefaultDeviceMemory(ref.getValue(), dev, size);
		}
	}

	/**
	 * Creates an adapter that allocates memory of the given page size.
	 * @param allocator		Delegate allocator
	 * @param page			Page size (bytes)
	 * @return Paged allocator
	 */
	static Allocator paged(Allocator allocator, long page) {
		return new Allocator() {
			@Override
			public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
				final long num = 1 + ((size - 1) / page);
				return allocator.allocate(type, num * page);
			}

			@Override
			public String toString() {
				return new ToStringBuilder(this).append(allocator).append("page", page).build();
			}
		};
	}
}
