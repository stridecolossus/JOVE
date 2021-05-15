package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkMemoryHeap;
import org.sarge.jove.platform.vulkan.VkMemoryHeapFlag;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryType;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MemoryTypeTest {
	private MemoryType type;
	private Heap heap;

	@BeforeEach
	void before() {
		heap = new Heap(0, 1, Set.of(VkMemoryHeapFlag.DEVICE_LOCAL));
		type = new MemoryType(0, heap, Set.of(VkMemoryPropertyFlag.DEVICE_LOCAL));
	}

	@Test
	void constructor() {
		assertEquals(0, type.index());
		assertEquals(heap, type.heap());
		assertEquals(Set.of(VkMemoryPropertyFlag.DEVICE_LOCAL), type.properties());
	}

	@Test
	void equals() {
		assertEquals(true, type.equals(type));
		assertEquals(true, type.equals(new MemoryType(0, heap, Set.of(VkMemoryPropertyFlag.DEVICE_LOCAL))));
		assertEquals(false, type.equals(null));
		assertEquals(false, type.equals(new MemoryType(0, heap, Set.of())));
	}

	@Nested
	class HeapTests {
		@Test
		void heap() {
			assertEquals(0, heap.index());
			assertEquals(1, heap.size());
			assertEquals(Set.of(VkMemoryHeapFlag.DEVICE_LOCAL), heap.flags());
			assertEquals(List.of(type), heap.types());
		}

		@Test
		void equals() {
			assertEquals(true, heap.equals(heap));
			assertEquals(false, heap.equals(null));
			assertEquals(false, heap.equals(new Heap(0, 1, Set.of())));
		}
	}

	@Test
	void extract() {
		// Create heap
		final var heap = new VkMemoryHeap();
		heap.size = 1;
		heap.flags = IntegerEnumeration.mask(VkMemoryHeapFlag.DEVICE_LOCAL);

		// Create memory type
		final var info = new VkMemoryType();
		info.heapIndex = 0;
		info.propertyFlags = IntegerEnumeration.mask(VkMemoryPropertyFlag.DEVICE_LOCAL);

		// Create memory properties
		final var props = new VkPhysicalDeviceMemoryProperties();
		props.memoryHeapCount = 1;
		props.memoryHeaps = new VkMemoryHeap[]{heap};
		props.memoryTypeCount = 1;
		props.memoryTypes = new VkMemoryType[]{info};

		// Extract from properties
		assertEquals(List.of(type), MemoryType.enumerate(props));
	}
}
