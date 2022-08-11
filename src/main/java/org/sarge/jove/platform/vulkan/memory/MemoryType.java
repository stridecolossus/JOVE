package org.sarge.jove.platform.vulkan.memory;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * A <i>memory type</i> specifies the properties of a type of memory supported by the hardware.
 * @author Sarge
 */
public record MemoryType(int index, MemoryType.Heap heap, Set<VkMemoryProperty> properties) {
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
	 * @param descriptor Memory properties descriptor
	 * @return Memory types
	 */
	public static MemoryType[] enumerate(VkPhysicalDeviceMemoryProperties descriptor) {
		// Extract heaps
		final Heap[] heaps = new Heap[descriptor.memoryHeapCount];
		final var heapMapper = IntegerEnumeration.reverse(VkMemoryHeapFlag.class);
		for(int n = 0; n < heaps.length; ++n) {
			final VkMemoryHeap heap = descriptor.memoryHeaps[n];
			final Set<VkMemoryHeapFlag> flags = heapMapper.enumerate(heap.flags);
			heaps[n] = new Heap(heap.size, flags);
		}

		// Extract memory types
		final MemoryType[] types = new MemoryType[descriptor.memoryTypeCount];
		final var typeMapper = IntegerEnumeration.reverse(VkMemoryProperty.class);
		for(int n = 0; n < types.length; ++n) {
			final VkMemoryType type = descriptor.memoryTypes[n];
			final Heap heap = heaps[type.heapIndex];
			final Set<VkMemoryProperty> props = typeMapper.enumerate(type.propertyFlags);
			types[n] = new MemoryType(n, heap, props);
		}

		return types;
	}
}
