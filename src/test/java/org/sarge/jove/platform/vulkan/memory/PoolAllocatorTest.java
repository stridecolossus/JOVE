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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.ByteSource.Sink;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.memory.PoolAllocator.Pool;

import com.sun.jna.Pointer;

public class PoolAllocatorTest {
	public static final long SIZE = 2;
	public static final int MAX = 3;

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

		// Create delegate allocator
		final Answer answer = inv -> {
			final long size = inv.getArgument(1);
			block = mock(DeviceMemory.class);
			when(block.size()).thenReturn(size);
			when(block.handle()).thenReturn(new Handle(new Pointer(size)));
			when(block.map(anyLong(), anyLong())).thenReturn(mock(Sink.class));
			return block;
		};
		block = null;
		delegate = mock(Allocator.class);
		when(delegate.allocate(eq(type), anyLong())).thenAnswer(answer);

		// Create pool allocator
		allocator = new PoolAllocator(delegate, MAX);
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
			pool.init(SIZE);
			assertEquals(SIZE, pool.size());
			assertEquals(SIZE, pool.free());
			assertEquals(0, pool.allocations().count());
			assertEquals(1, allocator.count());
			assertEquals(SIZE, allocator.size());
			assertEquals(SIZE, allocator.free());
		}

		@DisplayName("A memory instance can be allocated from the pool")
		@Test
		void allocate() {
			// Allocate memory
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			assertNotNull(mem);
			assertNotNull(mem.handle());
			assertEquals(SIZE, mem.size());
			assertEquals(false, mem.isDestroyed());

			// Check pool stats
			assertEquals(SIZE, pool.size());
			assertEquals(0, pool.free());
			assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());
		}

		@DisplayName("Allocated memory should be restored to the pool when it is released")
		@Test
		void release() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			pool.release();
			assertEquals(true, mem.isDestroyed());
			assertEquals(false, block.isDestroyed());
			assertEquals(SIZE, pool.free());
			assertEquals(SIZE, pool.size());
			assertEquals(1, allocator.count());
		}

		@DisplayName("Allocated memory should also be destroyed when the pool is destroyed")
		@Test
		void destroy() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			pool.destroy();
			verify(block).destroy();
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
			allocator.allocate(type, SIZE);
			assertEquals(SIZE, pool.size());
			assertEquals(0, pool.free());
			assertEquals(1, allocator.count());
			assertEquals(0, allocator.free());
			assertEquals(SIZE, allocator.size());
		}

		@DisplayName("Allocator should allocate from an existing block that has available free memory")
		@Test
		void allocateFromBlock() {
			pool.init(2 * SIZE);
			allocator.allocate(type, SIZE);
			allocator.allocate(type, SIZE);
			assertEquals(1, allocator.count());
			assertEquals(0, pool.free());
			assertEquals(2, pool.allocations().count());
		}

		@DisplayName("Allocator should allocate a new block if no existing blocks have sufficient free memory")
		@Test
		void allocateInsufficient() {
			pool.init(SIZE - 1);
			allocator.allocate(type, SIZE);
			assertEquals(2, allocator.count());
			assertEquals(SIZE - 1, pool.free());
		}

		@DisplayName("Allocator should reuse available released memory in the pool")
		@Test
		void allocateReallocate() {
			final DeviceMemory prev = allocator.allocate(type, SIZE);
			prev.destroy();
			assertEquals(prev, allocator.allocate(type, SIZE));
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
			for(int n = 0; n < MAX; ++n) {
				allocator.allocate(type, SIZE);
			}
			assertThrows(AllocationException.class, () -> allocator.allocate(type, SIZE));
		}

		@DisplayName("Allocator should fail if the delegate returns null")
		@Test
		void allocateDelegateNull() {
			when(delegate.allocate(type, SIZE)).thenReturn(null);
			assertThrows(AllocationException.class, () -> allocator.allocate(type, SIZE));
		}

		@DisplayName("Allocator should fail if the delegate returns memory that does not match the allocated size")
		@Test
		void allocateDelegateInvalidSize() {
			when(delegate.allocate(type, SIZE)).thenReturn(mock(DeviceMemory.class));
			assertThrows(AllocationException.class, () -> allocator.allocate(type, SIZE));
		}

		@DisplayName("Allocator should fail if the delegate throws an exception")
		@Test
		void allocateDelegateException() {
			when(delegate.allocate(type, SIZE)).thenThrow(RuntimeException.class);
			assertThrows(AllocationException.class, () -> allocator.allocate(type, SIZE));
		}
	}

	@Nested
	class ReleaseTests {
		@DisplayName("A destroyed memory instance is restored to the pool")
		@Test
		void destroy() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			mem.destroy();
			assertEquals(true, mem.isDestroyed());
			assertEquals(SIZE, pool.size());
			assertEquals(SIZE, pool.free());
			assertEquals(0, pool.allocations().count());
		}

		@DisplayName("A memory instance is destroyed if the associated block has been destroyed")
		@Test
		void destroyBlock() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			when(block.isDestroyed()).thenReturn(true);
			assertEquals(true, mem.isDestroyed());
		}

		@Test
		void destroyAlreadyDestroyed() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			mem.destroy();
			assertThrows(IllegalStateException.class, () -> mem.destroy());
		}
	}

	@Nested
	class MappedRegionTests {
		@Test
		void map() {
			final DeviceMemory mem = allocator.allocate(type, SIZE);
			mem.map();
			verify(block).map(SIZE, 0);
		}

		@DisplayName("All memory allocated from the same block should be mapped if any one is mapped")
		@Test
		void mapBlockMapped() {
			pool.init(2);
			when(block.isMapped()).thenReturn(true);
			final DeviceMemory one = allocator.allocate(type, 1);
			final DeviceMemory two = allocator.allocate(type, 1);
			assertEquals(true, one.isMapped());
			assertEquals(true, two.isMapped());
		}
	}

	@Nested
	class CleanupTests {
		private DeviceMemory mem;

		@BeforeEach
		void before() {
			mem = allocator.allocate(type, SIZE);
		}

		@DisplayName("Relasing the allocator should restore all allocated memory to the pool")
		@Test
		void release() {
			allocator.release();
			assertEquals(0, allocator.count());
			assertEquals(Map.of(type, pool), allocator.pools());
			assertEquals(true, mem.isDestroyed());
			assertEquals(SIZE, pool.size());
			assertEquals(SIZE, pool.free());
		}

		@DisplayName("Destroying the allocator should destroy all memory")
		@Test
		void destroy() {
			allocator.destroy();
			assertEquals(0, allocator.count());
			assertEquals(Map.of(type, pool), allocator.pools());
			verify(block).destroy();
			assertEquals(0, pool.size());
			assertEquals(0, pool.free());
		}
	}
}
