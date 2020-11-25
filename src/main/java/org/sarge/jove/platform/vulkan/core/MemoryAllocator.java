package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.MathsUtil;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>memory allocator</i> is used to allocate host and device memory for buffers, images, etc.
 * <p>
 * Usage:
 * <pre>
 *  // Create allocator
 *  MemoryAllocator allocator = MemoryAllocator.create(dev);
 *
 *  // Retrieve memory requirements from Vulkan
 *  VkMemoryRequirements reqs = ...
 *
 *  // Allocate memory
 *  Pointer mem = allocator
 *  	.allocation()
 *  	.init(reqs)
 *  	.size(size)
 *  	.allocate();
 * </pre>
 * @author Sarge
 */
public class MemoryAllocator {
	/**
	 * Creates a memory allocator for the given logical device.
	 * @param dev Logical device
	 * @return New memory allocator
	 */
	static MemoryAllocator create(LogicalDevice dev) {
		// Retrieve memory properties for this device
		final var props = new VkPhysicalDeviceMemoryProperties();
		dev.library().vkGetPhysicalDeviceMemoryProperties(dev.parent().handle(), props);

		// Create allocator
		return new MemoryAllocator(dev, props);
	}

	private final LogicalDevice dev;
	private final VkPhysicalDeviceMemoryProperties props;

	/**
	 * Constructor.
	 * @param dev			Logical device
	 * @param props			Memory properties
	 */
	private MemoryAllocator(LogicalDevice dev, VkPhysicalDeviceMemoryProperties props) {
		this.dev = notNull(dev);
		this.props = notNull(props);
	}

	/**
	 * @return New memory allocation
	 */
	public Allocation allocation() {
		return new Allocation();
	}

	/**
	 * Finds a memory type for the given memory properties.
	 * @param filter		Memory types filter mask
	 * @param mask	 		Memory properties bit-field
	 * @return Memory type index
	 * @throws RuntimeException if no suitable memory type is available
	 */
	private int findMemoryType(int filter, int mask) {
		// Find matching memory type index
		for(int n = 0; n < props.memoryTypeCount; ++n) {
			if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(props.memoryTypes[n].propertyFlags, mask)) {
				return n;
			}
		}

		// Otherwise memory not available for this device
		throw new RuntimeException("No memory type available for specified memory properties:" + props);
	}

	// TODO - stats

	// TODO - are these needed?
	//  VK_MAX_MEMORY_TYPES              = 32,
	//  VK_MAX_MEMORY_HEAPS              = 16,

	@Override
	public String toString() {
		// TODO - stats
		return new ToStringBuilder(this)
				.append("dev", dev)
				.build();
	}

	/**
	 * An <i>allocation</i> specifies memory requirements.
	 */
	public class Allocation {
		private long size;
		private int filter = Integer.MAX_VALUE;
		private final Set<VkMemoryPropertyFlag> flags = new HashSet<>();

		/**
		 * @return Allocation size (bytes)
		 */
		public long size() {
			return size;
		}

		/**
		 * Sets the required size of the memory.
		 * @param size Required memory size (bytes)
		 */
		public Allocation size(long size) {
			this.size = oneOrMore(size);
			return this;
		}

		/**
		 * Sets the memory type filter bit-mask.
		 * @param filter Memory type filter mask
		 */
		public Allocation filter(int filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Convenience method to initialise this allocation to the given memory requirements descriptor.
		 * @param reqs Memory requirements
		 */
		public Allocation init(VkMemoryRequirements reqs) {
			size(reqs.size);
			filter(reqs.memoryTypeBits);
			// TODO - alignment
			return this;
		}

		/**
		 * Adds a memory property.
		 * @param flag Memory property
		 */
		public Allocation property(VkMemoryPropertyFlag flag) {
			flags.add(notNull(flag));
			return this;
		}

		/**
		 * Allocates device memory.
		 * @return Memory handle
		 * @throws IllegalArgumentException if the memory size has not been specified
		 * @throws RuntimeException if the memory cannot be allocated
		 */
		public Pointer allocate() {
			// Validate
			if(size == 0) throw new IllegalArgumentException("Memory size not specified");

			// Find memory type
			final int mask = IntegerEnumeration.mask(flags);
			final int type = findMemoryType(filter, mask);

	        // TODO
	        // - limit number of allocations to maxMemoryAllocationCount
	        // - block allocation + offsets
	        // - pools = heap types
			// http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html
			// https://github.com/GPUOpen-LibrariesAndSDKs/VulkanMemoryAllocator

			// Init memory descriptor
			final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
	        info.allocationSize = size;
	        info.memoryTypeIndex = type;

	        // Allocate memory
	        final VulkanLibrary lib = dev.library();
	        final PointerByReference mem = lib.factory().pointer();
	        check(lib.vkAllocateMemory(dev.handle(), info, null, mem));

	        // Get memory handle
	        return mem.getValue();
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Allocation that) &&
					(this.size == that.size) &&
					(this.filter == that.filter) &&
					this.flags.equals(that.flags);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", size)
					.append("filter", filter)
					.append("flags", flags)
					.build();
		}
	}
}
