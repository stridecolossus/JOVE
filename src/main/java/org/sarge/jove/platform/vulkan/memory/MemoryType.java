package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.VkMemoryHeap;
import org.sarge.jove.platform.vulkan.VkMemoryHeapFlag;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * A <i>memory type</i> specifies the properties of a type of memory supported by the hardware.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record MemoryType(int index, Heap heap, Set<VkMemoryProperty> properties) {
	/**
	 * Constructor.
	 * @param index				Index
	 * @param heap				Heap
	 * @param properties		Memory properties
	 */
	public MemoryType {
		Check.zeroOrMore(index);
		Check.notNull(heap);
		properties = Set.copyOf(properties);
	}

	/**
	 * A <i>memory heap</i> specifies the properties of a group of memory types.
	 */
	public record Heap(long size, Set<VkMemoryHeapFlag> flags) {
		/**
		 * Constructor.
		 * @param size		Heap size
		 * @param flags		Flags
		 */
		public Heap {
			Check.zeroOrMore(size);
			flags = Set.copyOf(flags);
		}
	}

	/**
	 * Extracts the memory types supported by the hardware from the given descriptor.
	 * @param props Memory properties descriptor
	 * @return Memory types
	 */
	public static MemoryType[] enumerate(VkPhysicalDeviceMemoryProperties props) {
		// Extract heaps
		final Heap[] heaps = new Heap[props.memoryHeapCount];
		for(int n = 0; n < heaps.length; ++n) {
			final VkMemoryHeap heap = props.memoryHeaps[n];
			final Set<VkMemoryHeapFlag> flags = IntegerEnumeration.mapping(VkMemoryHeapFlag.class).enumerate(heap.flags);
			heaps[n] = new Heap(heap.size, flags);
		}

		// Extract memory types
		final MemoryType[] types = new MemoryType[props.memoryTypeCount];
		for(int n = 0; n < types.length; ++n) {
			final VkMemoryType type = props.memoryTypes[n];
			final Heap heap = heaps[type.heapIndex];
			final var properties = IntegerEnumeration.mapping(VkMemoryProperty.class).enumerate(type.propertyFlags);
			types[n] = new MemoryType(n, heap, properties);
		}

		return types;
	}
}
