package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class PoolAllocatorTest extends AbstractVulkanTest {
	private Allocator delegate;
	private PoolAllocator allocator;
	private MemoryType type;
	private AllocationPolicy policy;
	private DeviceMemory block;

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
		delegate = new DefaultAllocator(dev);
		allocator = new PoolAllocator(delegate, 1, policy);
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
//		assertEquals(block.handle(), mem.handle());
		assertEquals(1, allocator.count());
		assertEquals(1, allocator.size());
		assertEquals(0, allocator.free());
		verify(policy).apply(1, 0);
	}

	@Test
	void allocateMaximum() {
		allocator.allocate(type, 1);
		assertThrows(AllocationException.class, () -> allocator.allocate(type, 1));
	}

	@Test
	void allocatePolicy() {
		when(policy.apply(1, 0)).thenReturn(0L);
		allocator.allocate(type, 1);
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
	void destroy() {
		allocator.allocate(type, 1);
		allocator.destroy();
		assertEquals(0, allocator.count());
		assertEquals(0, allocator.size());
		assertEquals(0, allocator.free());
		assertEquals(1, allocator.pools().size());
	}

	@Test
	void create() {
		// TODO
	}
}
