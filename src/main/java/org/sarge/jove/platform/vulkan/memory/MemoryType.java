package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.util.*;
import java.util.stream.IntStream;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
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
	 * @param props Required memory properties
	 * @return Whether this memory type supports the given properties
	 */
	boolean matches(Set<VkMemoryProperty> props) {
		return properties.containsAll(props);
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
	 * @see PhysicalDevice#memory()
	 */
	public static MemoryType[] enumerate(VkPhysicalDeviceMemoryProperties descriptor) {
		class Helper {
			private final ReverseMapping<VkMemoryHeapFlag> mapper = new ReverseMapping<>(VkMemoryHeapFlag.class);
			private final ReverseMapping<VkMemoryProperty> properties = new ReverseMapping<>(VkMemoryProperty.class);
			private final List<Heap> heaps;

			/**
			 * Constructor.
			 * Enumerates the memory heaps.
			 */
			Helper() {
				heaps = Arrays
						.stream(descriptor.memoryHeaps)
						.map(this::heap)
						.toList();
			}

			/**
			 * Loads a memory heap.
			 */
			private Heap heap(VkMemoryHeap heap) {
				final Set<VkMemoryHeapFlag> flags = heap.flags.enumerate(mapper);
				return new Heap(heap.size, flags);
			}

			/**
			 * @return Memory types
			 */
			MemoryType[] types() {
				return IntStream
						.range(0, descriptor.memoryTypeCount)
						.mapToObj(this::type)
						.toArray(MemoryType[]::new);
			}

			/**
			 * Loads a memory type.
			 * @param index Memory type index
			 * @return Memory type
			 */
			private MemoryType type(int index) {
				final VkMemoryType type = descriptor.memoryTypes[index];
				final Heap heap = heaps.get(type.heapIndex);
				final Set<VkMemoryProperty> props = type.propertyFlags.enumerate(properties);
				return new MemoryType(index, heap, props);
			}
		}

		final Helper helper = new Helper();
		return helper.types();
	}
}
