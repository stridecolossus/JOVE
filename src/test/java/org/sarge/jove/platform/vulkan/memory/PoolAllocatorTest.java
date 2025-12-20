package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;

class PoolAllocatorTest {
	private PoolAllocator pool;

	@BeforeEach
	void before() {
		pool = new PoolAllocator(new MockAllocator(), 2);
	}

	@Test
	void constructor() {
		assertEquals(0, pool.count());
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(Map.of(), pool.pools());
	}

	@Test
	void pool() {
		final MemoryPool memoryPool = pool.pool(MockAllocator.MEMORY_TYPE);
		assertNotNull(memoryPool);
		assertEquals(0, memoryPool.blocks());
		assertEquals(0, memoryPool.size());
		assertEquals(0, memoryPool.free());
		assertEquals(Map.of(MockAllocator.MEMORY_TYPE, memoryPool), pool.pools());
		assertEquals(memoryPool, pool.pool(MockAllocator.MEMORY_TYPE));
	}

	@Test
	void preallocate() {
		pool.add(MockAllocator.MEMORY_TYPE, 1);
		assertEquals(1, pool.pool(MockAllocator.MEMORY_TYPE).free());
		assertEquals(1, pool.pool(MockAllocator.MEMORY_TYPE).blocks());
	}

	@Test
	void allocate() {
		final DeviceMemory mem = pool.allocate(MockAllocator.MEMORY_TYPE, 1);
		assertNotNull(mem);
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(1, pool.count());
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
	}

	@Test
	void release() {
		final DeviceMemory mem = pool.allocate(MockAllocator.MEMORY_TYPE, 1);
		pool.release();
		assertEquals(true, mem.isDestroyed());
		assertEquals(1, pool.count());
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(1, pool.pools().size());
	}

	@Test
	void destroy() {
		pool.allocate(MockAllocator.MEMORY_TYPE, 1);
		pool.destroy();
		assertEquals(0, pool.count());
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.pools().size());
	}
}
