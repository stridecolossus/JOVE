package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkMemoryHeapFlag.DEVICE_LOCAL;
import static org.sarge.jove.platform.vulkan.VkMemoryProperty.HOST_VISIBLE;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.util.EnumMask;

public class MemoryTypeTest {
	private MemoryType type;
	private Heap heap;

	@BeforeEach
	void before() {
		heap = new Heap(1, Set.of(DEVICE_LOCAL));
		type = new MemoryType(0, heap, Set.of(HOST_VISIBLE));
	}

	@Test
	void constructor() {
		assertEquals(0, type.index());
		assertEquals(heap, type.heap());
		assertEquals(Set.of(HOST_VISIBLE), type.properties());
		assertEquals(true, type.isHostVisible());
	}

	@Test
	void isHostVisible() {
		type = new MemoryType(0, heap, Set.of(VkMemoryProperty.HOST_COHERENT));
		assertEquals(false, type.isHostVisible());
	}

	@Test
	void matches() {
		assertEquals(true, type.matches(Set.of(HOST_VISIBLE)));
		assertEquals(false, type.matches(Set.of(VkMemoryProperty.PROTECTED)));
	}

	@Test
	void equals() {
		assertEquals(true, type.equals(type));
		assertEquals(true, type.equals(new MemoryType(0, heap, Set.of(HOST_VISIBLE))));
		assertEquals(false, type.equals(null));
		assertEquals(false, type.equals(new MemoryType(0, heap, Set.of())));
	}

	@Nested
	class HeapTests {
		@Test
		void heap() {
			assertEquals(1, heap.size());
			assertEquals(Set.of(DEVICE_LOCAL), heap.flags());
		}

		@Test
		void equals() {
			assertEquals(true, heap.equals(heap));
			assertEquals(false, heap.equals(null));
			assertEquals(false, heap.equals(new Heap(1, Set.of())));
		}
	}

	@Test
	void extract() {
		// Create heap
		final var heap = new VkMemoryHeap();
		heap.size = 1;
		heap.flags = EnumMask.of(DEVICE_LOCAL);

		// Create memory type
		final var info = new VkMemoryType();
		info.heapIndex = 0;
		info.propertyFlags = EnumMask.of(HOST_VISIBLE);

		// Create memory properties
		final var props = new VkPhysicalDeviceMemoryProperties();
		props.memoryHeapCount = 1;
		props.memoryHeaps = new VkMemoryHeap[]{heap};
		props.memoryTypeCount = 1;
		props.memoryTypes = new VkMemoryType[]{info};

		// Extract from properties
		assertArrayEquals(new MemoryType[]{type}, MemoryType.enumerate(props));
	}
}
