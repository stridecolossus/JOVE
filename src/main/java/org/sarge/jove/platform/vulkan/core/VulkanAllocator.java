package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.DeviceMemory;
import org.sarge.jove.common.DeviceMemory.AbstractDeviceMemory;
import org.sarge.jove.common.DeviceMemory.Pool;
import org.sarge.jove.common.DeviceMemory.Pool.AllocationException;
import org.sarge.jove.common.DeviceMemory.Pool.Allocator;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkMemoryAllocateInfo;
import org.sarge.jove.platform.vulkan.VkMemoryHeap;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.MathsUtil;

import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>memory allocator</i> is used to allocate device memory for buffers, images, etc.
 * <p>
 * Usage:
 * <pre>
 *  // Create allocator
 *  MemoryAllocator allocator = dev.allocator();
 *
 *  // Retrieve memory requirements
 *  VkMemoryRequirements reqs = ...
 *
 *  // Allocate memory
 *  DeviceMemory mem = allocator
 *      .request()
 *      .init(reqs)
 *      .required(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
 *      .optimal(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
 *      .optimal(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT)
 *      .allocate();
 *
 *  ...
 *
 *  // Release memory
 *  mem.destroy();
 * </pre>
 * @author Sarge
 */
// http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html
// https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html
public class VulkanAllocator {
	/**
	 * A <i>memory heap</i> specifies the properties of a group of device memory types.
	 */
	public static class Heap {
		private final int index;
		private final long size;
		private final Set<VkMemoryPropertyFlag> props;
		private final List<Type> types = new ArrayList<>();

		/**
		 * Constructor.
		 * @param index		Heap index
		 * @param size		Heap size
		 * @param props		Memory properties
		 */
		private Heap(int index, long size, Set<VkMemoryPropertyFlag> props) {
			this.index = zeroOrMore(index);
			this.size = oneOrMore(size);
			this.props = Set.copyOf(props);
		}

		/**
		 * @return Heap index
		 */
		public int index() {
			return index;
		}

		/**
		 * @return Heap size
		 */
		public long size() {
			return size;
		}

		/**
		 * @return Memory properties of this heap
		 */
		public Set<VkMemoryPropertyFlag> properties() {
			return props;
		}

		/**
		 * @return Memory types supported by this heap
		 */
		public List<Type> types() {
			return types;
		}

		@Override
		public int hashCode() {
			return Objects.hash(size, props, types);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Heap that) &&
					(this.size == that.size) &&
					this.props.equals(that.props) &&
					this.types.equals(that.types);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", size)
					.append("properties", props)
					.append("types", types.size())
					.build();
		}
	}

	/**
	 * A <i>memory type</i> defines a type of device memory supported by the hardware.
	 */
	public static class Type {
		private final int index;
		private final Heap heap;
		private final int props;
		private final PoolAllocator pool; // = new DeviceMemory.Pool(this::allocate);

		/**
		 * Constructor.
		 * @param index		Type index
		 * @param heap		Memory heap
		 * @param props		Properties
		 */
		private Type(int index, Heap heap, int props, Allocator allocator) {
			this.index = zeroOrMore(index);
			this.heap = notNull(heap);
			this.props = props;
			this.pool = new PoolAllocator(allocator);
			heap.types.add(this);

			//System.out.println(index+ " "+IntegerEnumeration.enumerate(VkMemoryPropertyFlag.class, props)+" "+heap);
		}

		/**
		 * @return Type index
		 */
		public int index() {
			return index;
		}

		/**
		 * @return Memory properties
		 */
		public Set<VkMemoryPropertyFlag> properties() {
			return IntegerEnumeration.enumerate(VkMemoryPropertyFlag.class, props);
		}

		/**
		 * @return Memory heap
		 */
		public Heap heap() {
			return heap;
		}

		/**
		 * @return Memory pool for this type
		 */
		public PoolAllocator pool() {
			return pool;
		}

//		/**
//		 * Allocates a new memory block.
//		 * @param type		Memory type
//		 * @param size		Size (bytes)
//		 * @return Memory
//		 * @throws VulkanException if the memory cannot be allocated
//		 */
//		private DeviceMemory allocate(long size) {
//			// Init memory descriptor
//			final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
//		    info.allocationSize = size;
//		    info.memoryTypeIndex = index;
//
//		    // Allocate memory
//		    final VulkanLibrary lib = dev.library();
//		    final PointerByReference ptr = lib.factory().pointer();
//		    check(lib.vkAllocateMemory(dev.handle(), info, null, ptr));
//
//		    // Create memory wrapper
//		    return new AbstractDeviceMemory(new Handle(ptr.getValue()), size, 0) {
//		    	@Override
//		    	protected void release() {
//		    		dev.library().vkFreeMemory(dev.handle(), handle, null);
//		    	}
//
//		    	@Override
//		    	protected void restore() {
//		    		throw new UnsupportedOperationException();
//		    	}
//		    };
//		}

		@Override
		public int hashCode() {
			return Integer.hashCode(index);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Type that) && (this.index == that.index);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("index", index)
					.append("heap", heap)
					.append("properties", properties())
					.build();
		}
	}

//	private final LogicalDevice dev;
	private final List<Heap> heaps;
	private final List<Type> types;
	private final int max;
//	private final long page;

	/**
	 * Constructor.
	 * @param dev Logical device
	 * @throws VulkanException if the allocator cannot be created
	 */
	public VulkanAllocator(LogicalDevice dev) {
		// Retrieve memory properties for this device
		final var props = new VkPhysicalDeviceMemoryProperties();
		dev.library().vkGetPhysicalDeviceMemoryProperties(dev.parent().handle(), props);

		// Retrieve global memory parameters
		final VkPhysicalDeviceLimits limits = dev.parent().properties().limits();

		// Enumerate memory heaps
		final List<Heap> heaps = new ArrayList<>();
		for(int n = 0; n < props.memoryHeapCount; ++n) {
			final VkMemoryHeap h = props.memoryHeaps[n];
			final var flags = IntegerEnumeration.enumerate(VkMemoryPropertyFlag.class, h.flags);
			final Heap heap = new Heap(n, h.size, flags);
			heaps.add(heap);
		}

		// Enumerate memory types array
		final Type[] types = new Type[props.memoryTypeCount];
		for(int n = 0; n < types.length; ++n) {
			final VkMemoryType info = props.memoryTypes[n];
			final Heap heap = heaps.get(info.heapIndex);
			final Allocator allocator = allocator(n, limits.bufferImageGranularity, dev);
			types[n] = new Type(n, heap, info.propertyFlags, allocator);
		}

		// Init allocator
//		this.dev = dev;
		this.heaps = List.copyOf(heaps);
		this.types = Arrays.asList(types);
		this.max = limits.maxMemoryAllocationCount;
//		this.page = limits.bufferImageGranularity;
	}

	/**
	 *
	 * @param type
	 * @param page
	 * @param dev
	 * @return
	 */
	protected Allocator allocator(int type, long page, LogicalDevice dev) {
		// Create allocator
		final Allocator allocator = size -> {
			// Init memory descriptor
			final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
		    info.allocationSize = size;
		    info.memoryTypeIndex = type;

		    // Allocate memory
		    final VulkanLibrary lib = dev.library();
		    final PointerByReference ptr = lib.factory().pointer();
		    check(lib.vkAllocateMemory(dev.handle(), info, null, ptr));

		    // Create memory wrapper
		    return new AbstractDeviceMemory(new Handle(ptr.getValue()), size, 0) {
		    	@Override
		    	protected void release() {
		    		dev.library().vkFreeMemory(dev.handle(), handle, null);
		    	}

		    	@Override
		    	protected void restore() {
		    		throw new UnsupportedOperationException();
		    	}
		    };
		};

		// Wrap as paged allocator
		return Allocator.paged(page, allocator);
	}

	/**
	 * @return Memory heaps
	 */
	public List<Heap> heaps() {
		return heaps;
	}

	/**
	 * @return Memory types
	 */
	public List<Type> types() {
		return types;
	}

	/**
	 * @return Maximum number of memory allocations supported by the hardware
	 */
	public int maximumAllocationCount() {
		return max;
	}

	/**
	 * Creates a new memory allocation request.
	 * @return New memory allocation request
	 */
	public Request request() {
		return new Request();
	}

	@Override
	public String toString() {
		// TODO
		// - allocations/max
		return super.toString();
	}

	/**
	 * An <i>memory allocation request</i> configures a request for a block of memory.
	 */
	public class Request {
		private long size;
		private int filter = Integer.MAX_VALUE;
		private final Set<VkMemoryPropertyFlag> optimal = new HashSet<>();
		private final Set<VkMemoryPropertyFlag> required = new HashSet<>();

		private Request() {
		}

		/**
		 * Sets the required size of the memory.
		 * @param size Required memory size (bytes)
		 */
		public Request size(long size) {
			this.size = oneOrMore(size);
			return this;
		}

		/**
		 * Sets the memory type filter bit-mask.
		 * @param filter Memory type filter mask
		 */
		public Request filter(int filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Convenience method to initialise this allocation to the given memory requirements descriptor.
		 * @param reqs Memory requirements
		 */
		public Request init(VkMemoryRequirements reqs) {
			size(reqs.size);
			filter(reqs.memoryTypeBits);
			// TODO - alignment
			return this;
		}

		/**
		 * Adds an <i>optimal</i> memory property.
		 * @param flag Optimal memory property
		 */
		public Request optimal(VkMemoryPropertyFlag flag) {
			optimal.add(notNull(flag));
			return this;
		}

		/**
		 * Adds a <i>required</i> memory property.
		 * @param flag Required memory property
		 */
		public Request required(VkMemoryPropertyFlag flag) {
			required.add(notNull(flag));
			return this;
		}

		/**
		 * Allocates device memory.
		 * @return Memory allocation
		 * @throws IllegalArgumentException if the memory size has not been specified
		 * @throws AllocationException if the memory cannot be allocated
		 */
		public DeviceMemory allocate() throws AllocationException {
			if(size == 0) throw new IllegalArgumentException("Memory size not specified");

			// Find optimal/required memory type for this request
			final Type type = find(optimal)
					.or(() -> find(required))
					.orElseThrow(() -> new AllocationException("No memory type available:" + this));

			//

			// Delegate to memory pool for this type
			return type.pool.allocate(size);
			// TODO - page
		}

		/**
		 * Finds the memory type for this request.
		 * @return Memory type
		 */
		private Optional<Type> find(Set<VkMemoryPropertyFlag> flags) {
			if(flags.isEmpty()) {
				return Optional.empty();
			}

			final int mask = IntegerEnumeration.mask(flags);
			return types
					.stream()
					.filter(type -> MathsUtil.isBit(filter, type.index))
					.filter(type -> MathsUtil.isMask(type.props, mask))
					.findAny();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", size)
					.append("filter", filter)
					.append("optimal", optimal)
					.append("required", required)
					.build();
		}
	}
}
