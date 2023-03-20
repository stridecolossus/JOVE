package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.common.*;

class PoolAllocatorTest {
	private Allocator allocator;
	private PoolAllocator pool;
	private MemoryType type;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		type = new MemoryType(0, new MemoryType.Heap(0, Set.of()), Set.of());
		allocator = new Allocator(dev, new MemoryType[]{type}, 1, 2);
		pool = new PoolAllocator(allocator, 3);
	}

	@Test
	void constructor() {
		assertEquals(0, pool.count());
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(2, pool.page());
		assertEquals(Map.of(), pool.pools());
	}

	@Test
	void pool() {
		final MemoryPool memoryPool = pool.pool(type);
		assertNotNull(memoryPool);
		assertEquals(0, memoryPool.count());
		assertEquals(0, memoryPool.size());
		assertEquals(0, memoryPool.free());
		assertEquals(Map.of(type, memoryPool), pool.pools());
		assertEquals(memoryPool, pool.pool(type));
	}

	@Test
	void allocate() {
		final DeviceMemory mem = pool.allocate(type, 1);
		assertNotNull(mem);
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(1, pool.count());
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
	}

	@Test
	void release() {
		final DeviceMemory mem = pool.allocate(type, 1);
		pool.release();
		assertEquals(true, mem.isDestroyed());
		assertEquals(1, pool.count());
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(1, pool.pools().size());
	}

	@Test
	void destroy() {
		pool.allocate(type, 1);
		pool.destroy();
		assertEquals(0, pool.count());
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.pools().size());
	}
}
