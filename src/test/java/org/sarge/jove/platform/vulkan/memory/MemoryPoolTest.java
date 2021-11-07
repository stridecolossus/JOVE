package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MemoryPoolTest {
	private MemoryPool pool;
	private MemoryType type;
	private Allocator allocator;
	private DeviceMemory block, larger;

	@BeforeEach
	void before() {
		// Create memory block
		block = mock(DeviceMemory.class);
		when(block.handle()).thenReturn(new Handle(1));
		when(block.size()).thenReturn(1L);

		// Create larger memory block
		larger = mock(DeviceMemory.class);
		when(larger.handle()).thenReturn(new Handle(3));
		when(larger.size()).thenReturn(3L);

		// Define memory type
		type = new MemoryType(0, new Heap(0, 0, Set.of()), Set.of());

		// Create underlying allocator
		allocator = mock(Allocator.class);
		when(allocator.allocate(type, 1)).thenReturn(block);
		when(allocator.allocate(type, 3)).thenReturn(larger);

		// Create pool
		pool = new MemoryPool(type, allocator);
	}

	@Test
	void constructor() {
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(0, pool.count());
		assertNotNull(pool.allocations());
		assertEquals(0, pool.allocations().count());
	}

	@Test
	void init() {
		pool.init(1);
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(1, pool.count());
		assertEquals(0, pool.allocations().count());
	}

	@DisplayName("Released memory should be returned to the pool")
	@Test
	void destroyAllocatedMemory() {
		final DeviceMemory mem = pool.allocate(1);
		mem.destroy();
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(1, pool.count());
		assertEquals(0, pool.allocations().count());
	}

	@DisplayName("An empty pool should allocate a new block")
	@Test
	void allocateNewBlock() {
		// Allocate memory from a newly allocated block
		final DeviceMemory mem = pool.allocate(1);
		assertNotNull(mem);
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(block.handle(), mem.handle());

		// Check new block allocated
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.count());
		assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());
		verify(allocator).allocate(type, 1);
	}

	@DisplayName("An empty pool should allocate a new block which may be larger than the requested size")
	@Test
	void allocateNewLargerBlock() {
		when(allocator.allocate(type, 1)).thenReturn(larger);
		final DeviceMemory mem = pool.allocate(1);
		assertEquals(larger.handle(), mem.handle());
		assertEquals(1, mem.size());
		assertEquals(3, pool.size());
		assertEquals(2, pool.free());
		assertEquals(1, pool.count());
	}

	@DisplayName("Memory should be allocated from an existing block with the requested size")
	@Test
	void allocateExistingBlock() {
		pool.init(1);
		assertEquals(1, pool.allocate(1).size());
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.count());
	}

	@DisplayName("Memory should be allocated from an existing block with available free memory")
	@Test
	void allocateExistingLargerBlock() {
		pool.init(3);
		assertEquals(1, pool.allocate(1).size());
		assertEquals(3, pool.size());
		assertEquals(2, pool.free());
		assertEquals(1, pool.count());
	}

	@DisplayName("Memory should be reallocated if available")
	@Test
	void allocateReallocatedMemory() {
		// Add a large block
		pool.init(3);

		// Allocate then destroy some memory
		final DeviceMemory mem = pool.allocate(1);
		mem.destroy();
		assertEquals(true, mem.isDestroyed());

		// Allocate and destroy some more memory that is larger
		pool.allocate(2).destroy();

		// Allocate and check reallocates the memory with nearest size
		assertEquals(mem, pool.allocate(1));
		assertEquals(false, mem.isDestroyed());
		assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());

		// Check reallocated memory
		assertEquals(3, pool.size());
		assertEquals(2, pool.free());
		assertEquals(1, pool.count());
	}

	@DisplayName("Allocation should fail if the underlying allocator fails")
	@Test
	void allocateFails() {
		when(allocator.allocate(type, 1)).thenThrow(new AllocationException("doh"));
		assertThrows(AllocationException.class, "doh", () -> pool.allocate(1));
	}

	@DisplayName("Allocation should fail if the underlying allocator returns a NULL block")
	@Test
	void allocateReturnsNull() {
		when(allocator.allocate(type, 1)).thenReturn(null);
		assertThrows(AllocationException.class, "Allocator returned NULL", () -> pool.allocate(1));
	}

	@DisplayName("Allocation should fail if the underlying allocator returns a block that is too small")
	@Test
	void allocateReturnsSmallerBlock() {
		when(allocator.allocate(type, 2)).thenReturn(block);
		assertThrows(AllocationException.class, "smaller than the requested size", () -> pool.allocate(2));
	}

	@DisplayName("Releasing the pool should restore all memory back to the pool")
	@Test
	void release() {
		final DeviceMemory mem = pool.allocate(1);
		pool.release();
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(1, pool.count());
		assertEquals(0, pool.allocations().count());
		assertEquals(true, mem.isDestroyed());
	}

	@DisplayName("Destroying the pool should destroy all allocated blocks")
	@Test
	void close() {
		final DeviceMemory mem = pool.allocate(1);
		pool.close();
		when(block.isDestroyed()).thenReturn(true);
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(0, pool.count());
		assertEquals(0, pool.allocations().count());
		assertEquals(true, mem.isDestroyed());
	}
}
