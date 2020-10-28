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
 * TODO - doc
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

	/**
	 * An <i>allocation</i> specifies memory requirements.
	 */
	public record Allocation(long size, int filter, Set<VkMemoryPropertyFlag> flags) {
		/**
		 * Builder for an allocation.
		 */
		public static class Builder {
			private final Set<VkMemoryPropertyFlag> flags = new HashSet<>();
			private long size;
			private int filter = Integer.MAX_VALUE;

			/**
			 * Sets the required size of the memory.
			 * @param size Required memory size (bytes)
			 */
			public Builder size(long size) {
				this.size = oneOrMore(size);
				return this;
			}

			/**
			 * Sets the memory type filter bit-mask.
			 * @param filter Memory type filter mask
			 */
			public Builder filter(int filter) {
				this.filter = filter;
				return this;
			}

			/**
			 * Convenience setter to initialise this allocation to the given memory requirements descriptor.
			 * @param reqs Memory requirements
			 */
			public Builder init(VkMemoryRequirements reqs) {
				size(reqs.size);
				filter(reqs.memoryTypeBits);
				// TODO - alignment
				return this;
			}

			/**
			 * Adds a memory property.
			 * @param flag Memory property
			 */
			public Builder property(VkMemoryPropertyFlag flag) {
				flags.add(notNull(flag));
				return this;
			}

			/**
			 * Constructs this allocation.
			 * @return New memory allocation descriptor
			 * @throws IllegalArgumentException if the size has not been specified
			 */
			public Allocation build() {
				if(size == 0) throw new IllegalArgumentException("Memory size has not been specified");
				return new Allocation(size, filter, flags);
			}
		}
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
	 * Allocates device memory.
	 * @param allocation Allocation descriptor
	 * @return Memory handle
	 * @throws RuntimeException if the memory cannot be allocated
	 */
	Pointer allocate(Allocation allocation) {
		// Find memory type
		final int mask = IntegerEnumeration.mask(allocation.flags);
		final int type = findMemoryType(allocation.filter, mask);

        // TODO
        // - limit number of allocations to maxMemoryAllocationCount
        // - block allocation + offsets
        // - pools = heap types
		// http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html
		// https://github.com/GPUOpen-LibrariesAndSDKs/VulkanMemoryAllocator

		// Init memory descriptor
		final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
        info.allocationSize = allocation.size;
        info.memoryTypeIndex = type;

        // Allocate memory
        final VulkanLibrary lib = dev.library();
        final PointerByReference mem = lib.factory().pointer();
        check(lib.vkAllocateMemory(dev.handle(), info, null, mem));

        // Get memory handle
        return mem.getValue();
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

	@Override
	public String toString() {
		// TODO - stats
		return new ToStringBuilder(this)
				.append("dev", dev)
				.build();
	}
}
