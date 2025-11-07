package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.util.*;
import java.util.function.IntFunction;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>memory type</i> specifies the properties of the memory heaps supported by the hardware.
 * @author Sarge
 */
public record MemoryType(int index, MemoryType.Heap heap, Set<VkMemoryProperty> properties) {
	/**
	 * Constructor.
	 * @param index				Type index
	 * @param heap				Heap
	 * @param properties		Memory properties
	 */
	public MemoryType {
		requireNonNull(heap);
		properties = Set.copyOf(properties);
	}

	/**
	 * @return Whether this memory type is {@link VkMemoryProperty#HOST_VISIBLE}
	 */
	public boolean isHostVisible() {
		return properties.contains(VkMemoryProperty.HOST_VISIBLE);
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
			requireZeroOrMore(size);
			flags = Set.copyOf(flags);
		}
	}

	/**
	 * Extracts the memory types supported by the hardware from the given descriptor.
	 * @param descriptor Memory properties descriptor
	 * @return Memory types
	 */
	public static MemoryType[] enumerate(VkPhysicalDeviceMemoryProperties descriptor) {
		// Extracts a memory heap
		class HeapMapper implements IntFunction<Heap> {
			private final ReverseMapping<VkMemoryHeapFlag> mapper = ReverseMapping.mapping(VkMemoryHeapFlag.class);

			@Override
			public Heap apply(int index) {
				final VkMemoryHeap heap = descriptor.memoryHeaps[index];
    			final Set<VkMemoryHeapFlag> flags = heap.flags.enumerate(mapper);
    			return new Heap(heap.size, flags);
			}
		}

		// Extract heaps
		final Heap[] heaps = new Heap[descriptor.memoryHeapCount];
		Arrays.setAll(heaps, new HeapMapper());

		// Extracts a memory type
		class TypeMapper implements IntFunction<MemoryType> {
			private final ReverseMapping<VkMemoryProperty> properties = ReverseMapping.mapping(VkMemoryProperty.class);

			@Override
			public MemoryType apply(int index) {
				final VkMemoryType type = descriptor.memoryTypes[index];
				final Heap heap = heaps[type.heapIndex];
				final Set<VkMemoryProperty> props = type.propertyFlags.enumerate(properties);
				return new MemoryType(index, heap, props);
			}
		}

		// Extract memory types
		final MemoryType[] types = new MemoryType[descriptor.memoryTypeCount];
		Arrays.setAll(types, new TypeMapper());

		return types;
	}
}
