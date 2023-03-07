package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;

class PoolAllocatorTest {
	private Allocator delegate;
	private PoolAllocator pool;
	private MemoryType type;
	private AllocationPolicy policy;
	private DeviceMemory block;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		// Create a memory block
		block = mock(DeviceMemory.class);
		when(block.size()).thenReturn(1L);

		// Create underlying allocator
		type = new MemoryType(0, new MemoryType.Heap(0, Set.of()), Set.of());

		// Create allocation policy
		policy = mock(AllocationPolicy.class);
		when(policy.apply(1, 0)).thenReturn(1L);

		// Create pool
		dev = new MockDeviceContext();
		delegate = new DefaultAllocator(dev);
		pool = new PoolAllocator(delegate, 1, policy);
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
		verify(policy).apply(1, 0);
	}

	@Test
	void allocateMaximum() {
		pool.allocate(type, 1);
		assertThrows(AllocationException.class, () -> pool.allocate(type, 1));
	}

	@Test
	void allocatePolicy() {
		when(policy.apply(1, 0)).thenReturn(0L);
		pool.allocate(type, 1);
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
