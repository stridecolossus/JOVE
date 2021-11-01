package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.memory.PoolAllocator.Pool;

public class PoolAllocatorTest {
	private PoolAllocator allocator;
	private MemoryType type;
	private Pool pool;
	private Allocator delegate;
	private DeviceMemory block;

	@BeforeEach
	void before() {
		// Create a memory type
		final Heap heap = new Heap(0, 0, Set.of());
		type = new MemoryType(1, heap, Set.of());

		// Create a memory block
		block = null;
		final Answer<DeviceMemory> answer = inv -> {
			final long size = inv.getArgument(1);
			block = mock(DeviceMemory.class);
			when(block.size()).thenReturn(size);
			assertEquals(type, inv.getArgument(0));
			return block;
		};

		// Init underlying allocator
		delegate = mock(Allocator.class);
		when(delegate.allocate(eq(type), anyLong())).thenAnswer(answer);

		// Create pool allocator
		allocator = new PoolAllocator(delegate, 3);

		// Retrieve memory pool
		pool = allocator.pool(type);
	}

	@Test
	void constructor() {
		assertEquals(0, allocator.count());
		assertEquals(0, allocator.free());
		assertEquals(0, allocator.size());
	}

	@Nested
	class PoolTests {
		@Test
		void pool() {
			assertNotNull(pool);
			assertEquals(0, pool.size());
			assertEquals(0, pool.free());
			assertNotNull(pool.allocations());
			assertEquals(0, pool.allocations().count());
			assertEquals(Map.of(type, pool), allocator.pools());
		}

		@DisplayName("Memory can be pre-allocated to the pool")
		@Test
		void init() {
			pool.init(2);
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
			assertEquals(0, pool.allocations().count());
			assertEquals(1, allocator.count());
			assertEquals(2, allocator.size());
			assertEquals(2, allocator.free());
		}

		@DisplayName("A memory instance can be allocated from the pool")
		@Test
		void allocate() {
			// Allocate memory
			final DeviceMemory mem = allocator.allocate(type, 2);
			assertNotNull(mem);
			assertEquals(2, mem.size());
			assertEquals(false, mem.isDestroyed());
			assertEquals(Optional.empty(), mem.region());

			// Check pool stats
			assertEquals(2, pool.size());
			assertEquals(0, pool.free());
			assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());
		}

		@DisplayName("Allocated memory should be restored to the pool when it is released")
		@Test
		void release() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			pool.release();
			assertEquals(true, mem.isDestroyed());
			assertEquals(false, block.isDestroyed());
			assertEquals(2, pool.free());
			assertEquals(2, pool.size());
			assertEquals(1, allocator.count());
		}

		@DisplayName("Allocated memory should also be destroyed when the pool is destroyed")
		@Test
		void close() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			pool.close();
			verify(block).close();
			assertEquals(0, pool.free());
			assertEquals(0, pool.size());
			assertEquals(0, allocator.count());
		}
	}

	@Nested
	class AllocationTests {
		@DisplayName("Allocator should create a new block when the pool is empty")
		@Test
		void allocateEmpty() {
			allocator.allocate(type, 2);
			assertEquals(2, pool.size());
			assertEquals(0, pool.free());
			assertEquals(1, allocator.count());
			assertEquals(0, allocator.free());
			assertEquals(2, allocator.size());
		}

		@DisplayName("Allocator should allocate from an existing block that has available free memory")
		@Test
		void allocateFromBlock() {
			pool.init(3);
			allocator.allocate(type, 2);
			assertEquals(1, allocator.count());
			assertEquals(1, pool.free());
			assertEquals(1, pool.allocations().count());
		}

		@DisplayName("Allocator should allocate a new block if no existing blocks have sufficient free memory")
		@Test
		void allocateInsufficient() {
			pool.init(1);
			allocator.allocate(type, 2);
			assertEquals(2, allocator.count());
			assertEquals(1, pool.free());
		}

		@DisplayName("Allocator should reuse available released memory in the pool")
		@Test
		void allocateReallocate() {
			final DeviceMemory prev = allocator.allocate(type, 2);
			prev.close();
			assertEquals(prev, allocator.allocate(type, 2));
			assertEquals(1, allocator.count());
		}

		@DisplayName("Allocator should create a new block when the pool has sufficient free memory but it is fragmented")
		@Test
		void allocateFragmented() {
			// Add two blocks
			pool.init(3);
			pool.init(3);

			// Allocate memory from each block leaving fragmented free space
			allocator.allocate(type, 2);
			allocator.allocate(type, 2);
			assertEquals(2, allocator.count());
			assertEquals(2, allocator.free());

			// Allocate again and check new block is created
			allocator.allocate(type, 2);
			assertEquals(3, allocator.count());
			assertEquals(2, allocator.free());
		}

		@DisplayName("Allocator should fail if the maximum allocations is exceeded")
		@Test
		void max() {
			for(int n = 0; n < 3; ++n) {
				allocator.allocate(type, 1);
			}
			assertThrows(AllocationException.class, () -> allocator.allocate(type, 1));
		}

		@DisplayName("Allocator should fail if the delegate returns null")
		@Test
		void allocateDelegateNull() {
			when(delegate.allocate(type, 2)).thenReturn(null);
			assertThrows(AllocationException.class, () -> allocator.allocate(type, 2));
		}

		@DisplayName("Allocator should fail if the delegate returns memory that does not match the allocated size")
		@Test
		void allocateDelegateInvalidSize() {
			when(delegate.allocate(type, 2)).thenReturn(mock(DeviceMemory.class));
			assertThrows(AllocationException.class, () -> allocator.allocate(type, 2));
		}

		@DisplayName("Allocator should fail if the delegate throws an exception")
		@Test
		void allocateDelegateException() {
			when(delegate.allocate(type, 2)).thenThrow(RuntimeException.class);
			assertThrows(AllocationException.class, () -> allocator.allocate(type, 2));
		}
	}

	@Nested
	class ReleaseTests {
		@DisplayName("A destroyed memory instance is restored to the pool")
		@Test
		void destroy() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			mem.close();
			assertEquals(true, mem.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
			assertEquals(0, pool.allocations().count());
		}

		@DisplayName("A memory instance is destroyed if the associated block has been destroyed")
		@Test
		void destroyBlock() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			when(block.isDestroyed()).thenReturn(true);
			assertEquals(true, mem.isDestroyed());
		}

		@Test
		void destroyAlreadyDestroyed() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			mem.close();
			assertThrows(IllegalStateException.class, () -> mem.close());
		}
	}

	@Nested
	class MappedRegionTests {
		private Region region;

		@BeforeEach
		void before() {
			region = mock(Region.class);
		}

		@Test
		void map() {
			final DeviceMemory mem = allocator.allocate(type, 2);
			mem.map();
			verify(block).map(0, 2);
			when(block.region()).thenReturn(Optional.of(region));
			assertEquals(Optional.of(region), mem.region());
		}

		@DisplayName("All memory allocated from the same block should be mapped if any one is mapped")
		@Test
		void mapBlockMapped() {
			// Allocate from block and map
			pool.init(2);
			final DeviceMemory one = allocator.allocate(type, 1);
			one.map(0, 1);

			// Allocate again from block and map
			final DeviceMemory two = allocator.allocate(type, 1);
			when(block.region()).thenReturn(Optional.of(region));
			two.map(0, 1);

			// Check region unmapped
			verify(region).unmap();

			// Check both memory refer to the common mapped region
			assertEquals(Optional.of(region), one.region());
			assertEquals(Optional.of(region), one.region());
		}
	}

	@Nested
	class CleanupTests {
		private DeviceMemory mem;

		@BeforeEach
		void before() {
			mem = allocator.allocate(type, 2);
		}

		@DisplayName("Relasing the allocator should restore all allocated memory to the pool")
		@Test
		void release() {
			allocator.release();
			assertEquals(0, allocator.count());
			assertEquals(Map.of(type, pool), allocator.pools());
			assertEquals(true, mem.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
		}

		@DisplayName("Destroying the allocator should destroy all memory")
		@Test
		void close() {
			allocator.close();
			assertEquals(0, allocator.count());
			assertEquals(Map.of(type, pool), allocator.pools());
			verify(block).close();
			assertEquals(0, pool.size());
			assertEquals(0, pool.free());
		}
	}
}
