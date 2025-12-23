package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;

class PoolAllocatorTest {
	private PoolAllocator allocator;

	@BeforeEach
	void before() {
		allocator = new PoolAllocator(new MockAllocator(), 2);
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
		final MemoryPool memoryPool = allocator.pool(MockAllocator.MEMORY_TYPE);
		assertNotNull(memoryPool);
		assertEquals(0, memoryPool.blocks());
		assertEquals(0, memoryPool.size());
		assertEquals(0, memoryPool.free());
		assertEquals(Map.of(MockAllocator.MEMORY_TYPE, memoryPool), allocator.pools());
		assertEquals(memoryPool, allocator.pool(MockAllocator.MEMORY_TYPE));
	}

	@Test
	void preallocate() {
		allocator.add(MockAllocator.MEMORY_TYPE, 1);
		assertEquals(1, allocator.pool(MockAllocator.MEMORY_TYPE).free());
		assertEquals(1, allocator.pool(MockAllocator.MEMORY_TYPE).blocks());
	}

	@Test
	void allocate() {
		final DeviceMemory mem = allocator.allocate(MockAllocator.MEMORY_TYPE, 1);
		assertNotNull(mem);
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());
		assertEquals(1, allocator.count());
		assertEquals(1, allocator.size());
		assertEquals(0, allocator.free());
	}

	@Test
	void release() {
		final DeviceMemory mem = allocator.allocate(MockAllocator.MEMORY_TYPE, 1);
		allocator.release();
		assertEquals(true, mem.isDestroyed());
		assertEquals(1, allocator.count());
		assertEquals(1, allocator.size());
		assertEquals(1, allocator.free());
		assertEquals(1, allocator.pools().size());
	}

	@Test
	void destroy() {
		allocator.allocate(MockAllocator.MEMORY_TYPE, 1);
		allocator.destroy();
		assertEquals(0, allocator.size());
		assertEquals(0, allocator.free());
		assertEquals(1, allocator.pools().size());
	}
}
