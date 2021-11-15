package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
	 * @param index				Memory type index
	 * @param heap				Heap
	 * @param properties		Memory properties
	 */
	public MemoryType {
		Check.zeroOrMore(index);
		properties = Check.notNull(properties);
		heap.types.add(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("index", index)
				.append("heap", heap.index)
				.append(properties)
				.build();
	}

	/**
	 * A <i>memory heap</i> specifies the properties of a group of memory types.
	 */
	public static class Heap {
		private final int index;
		private final long size;
		private final Set<VkMemoryHeapFlag> flags;
		private final transient List<MemoryType> types = new ArrayList<>();

		/**
		 * Constructor.
		 * @param index		Heap index
		 * @param size		Size
		 * @param flags		Memory flags
		 */
		public Heap(int index, long size, Set<VkMemoryHeapFlag> flags) {
			this.index = zeroOrMore(index);
			this.size = zeroOrMore(size);
			this.flags = Set.copyOf(flags);
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
		 * @return Memory flags for this heap
		 */
		public Set<VkMemoryHeapFlag> flags() {
			return flags;
		}

		/**
		 * @return Memory types in this heap
		 */
		public List<MemoryType> types() {
			return List.copyOf(types);
		}

		@Override
		public int hashCode() {
			return Objects.hash(index, size, flags);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Heap that) &&
					(this.index == that.index) &&
					(this.size == that.size) &&
					this.flags.equals(that.flags);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("index", index)
					.append("size", size)
					.append("flags", flags)
					.build();
		}
	}

	/**
	 * Extracts the memory types supported by the hardware from the given descriptor.
	 * @param props Memory properties descriptor
	 * @return Memory types
	 */
	public static List<MemoryType> enumerate(VkPhysicalDeviceMemoryProperties props) {
		// Init heaps and types
		final Heap[] heaps = new Heap[props.memoryHeapCount];
		final MemoryType[] types = new MemoryType[props.memoryTypeCount];

		// Helper
		class Mapper {
			Heap heap(int index) {
				final VkMemoryHeap heap = props.memoryHeaps[index];
				final var flags = IntegerEnumeration.mapping(VkMemoryHeapFlag.class).enumerate(heap.flags);
				return new Heap(index, heap.size, flags);
			}

			MemoryType type(int index) {
				final VkMemoryType type = props.memoryTypes[index];
				final Heap heap = heaps[type.heapIndex];
				final var properties = IntegerEnumeration.mapping(VkMemoryProperty.class).enumerate(type.propertyFlags);
				return new MemoryType(index, heap, properties);
			}
		}

		// Enumerate memory heaps and types
		final Mapper mapper = new Mapper();
		Arrays.setAll(heaps, mapper::heap);
		Arrays.setAll(types, mapper::type);

		// Convert to collection
		return Arrays.asList(types);
	}
}
