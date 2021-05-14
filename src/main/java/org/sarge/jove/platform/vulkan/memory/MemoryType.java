package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkMemoryHeap;
import org.sarge.jove.platform.vulkan.VkMemoryHeapFlag;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.lib.util.Check;

/**
 * A <i>memory type</i> specifies the properties of a type of memory supported by the hardware.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record MemoryType(int index, Heap heap, Set<VkMemoryPropertyFlag> properties) {
	/**
	 * Constructor.
	 * @param index				Memory type index
	 * @param heap				Heap
	 * @param properties		Memory properties
	 */
	public MemoryType {
		Check.zeroOrMore(index);
		Check.notNull(properties);
		heap.types.add(this);
	}

	/**
	 * A <i>memory heap</i> specifies the properties of a group of device memory types.
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
	 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html">Vulkan documentation</a>
	 */
	public static List<MemoryType> enumerate(VkPhysicalDeviceMemoryProperties props) {
		// Enumerate memory heaps
		final IntFunction<Heap> heapMapper = index -> {
			final VkMemoryHeap heap = props.memoryHeaps[index];
			final var flags = IntegerEnumeration.enumerate(VkMemoryHeapFlag.class, heap.flags);
			return new Heap(index, heap.size, flags);
		};
		final Heap[] heaps = new Heap[props.memoryHeapCount];
		Arrays.setAll(heaps, heapMapper);

		// Enumerate memory types
		final IntFunction<MemoryType> typeMapper = index -> {
			final VkMemoryType type = props.memoryTypes[index];
			final Heap heap = heaps[type.heapIndex];
			final var properties = IntegerEnumeration.enumerate(VkMemoryPropertyFlag.class, type.propertyFlags);
			return new MemoryType(index, heap, properties);
		};
		final MemoryType[] types = new MemoryType[props.memoryTypeCount];
		Arrays.setAll(types, typeMapper);

		// Convert to collection
		return Arrays.asList(types);
	}
}
