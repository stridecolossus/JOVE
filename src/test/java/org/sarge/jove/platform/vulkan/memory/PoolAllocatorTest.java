package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PoolAllocatorTest {
	private PoolAllocator allocator;
	private Allocator delegate;
	private MemoryType type;
	private DeviceMemory block;

	@BeforeEach
	void before() {
		// Create a memory block
		block = mock(DeviceMemory.class);
		when(block.size()).thenReturn(1L);

		// Create underlying allocator
		type = new MemoryType(0, new MemoryType.Heap(0, 0, Set.of()), Set.of());
		delegate = mock(Allocator.class);
		when(delegate.allocate(type, 1)).thenReturn(block);

		// Create pool
		allocator = new PoolAllocator(delegate, 1);
	}

	@Test
	void constructor() {
		assertEquals(0, allocator.count());
		assertEquals(0, allocator.size());
		assertEquals(0, allocator.free());
		assertEquals(Map.of(), allocator.pools());
	}

	@Test
	void pool() {
		final MemoryPool pool = allocator.pool(type);
		assertNotNull(pool);
		assertEquals(0, pool.count());
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(Map.of(type, pool), allocator.pools());
		assertEquals(pool, allocator.pool(type));
	}

	@Test
	void allocate() {
		final DeviceMemory mem = allocator.allocate(type, 1);
		assertNotNull(mem);
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(block.handle(), mem.handle());
		assertEquals(1, allocator.count());
		assertEquals(1, allocator.size());
		assertEquals(0, allocator.free());
	}

	@Test
	void release() {
		final DeviceMemory mem = allocator.allocate(type, 1);
		allocator.release();
		assertEquals(true, mem.isDestroyed());
		assertEquals(1, allocator.count());
		assertEquals(1, allocator.size());
		assertEquals(1, allocator.free());
		assertEquals(1, allocator.pools().size());
	}

	@Test
	void close() {
		allocator.allocate(type, 1);
		allocator.close();
		assertEquals(0, allocator.count());
		assertEquals(0, allocator.size());
		assertEquals(0, allocator.free());
		assertEquals(1, allocator.pools().size());
	}
}
