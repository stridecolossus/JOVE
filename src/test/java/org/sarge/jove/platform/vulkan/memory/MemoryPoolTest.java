package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.*;

class MemoryPoolTest {
	private MemoryPool pool;
	private Allocator allocator;

	@BeforeEach
	void before() {
		allocator = new MockAllocator();
		pool = new MemoryPool(MockAllocator.MEMORY_TYPE);
	}

	@DisplayName("A new pool...")
	@Nested
	class Empty {
    	@DisplayName("is initially empty")
    	@Test
    	void empty() {
    		assertEquals(0, pool.size());
    		assertEquals(0, pool.free());
    		assertEquals(0, pool.blocks());
    		assertEquals(0, pool.allocations().count());
    	}

    	@DisplayName("cannot allocate memory")
    	@Test
    	void allocate() {
    		assertEquals(Optional.empty(), pool.allocate(1));
    	}

		@Disabled
    	@DisplayName("cannot reallocate memory")
    	@Test
    	void reallocate() {
    		assertEquals(Optional.empty(), pool.reallocate(1));
    	}

    	@DisplayName("can add new memory blocks")
    	@Test
    	void add() {
    		pool.add(new Block(allocator.allocate(MockAllocator.MEMORY_TYPE, 2)));
    		assertEquals(2, pool.size());
    		assertEquals(2, pool.free());
    		assertEquals(1, pool.blocks());
    		assertEquals(0, pool.allocations().count());
    	}
	}

	@DisplayName("A pool with a free memory block...")
	@Nested
	class Free {
		private Block block;

		@BeforeEach
		void before() {
			block = new Block(allocator.allocate(MockAllocator.MEMORY_TYPE, 2));
    		pool.add(block);
		}

		@DisplayName("can allocate from that block")
		@Test
		void allocate() {
			final DeviceMemory memory = pool.allocate(1).get();
			assertEquals(1, memory.size());
			assertEquals(MockAllocator.MEMORY_TYPE, memory.type());
			assertEquals(false, memory.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(1, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(1, pool.allocations().count());
		}

		@DisplayName("cannot allocate if there are no blocks with sufficient free memory")
		@Test
		void none() {
			assertEquals(Optional.empty(), pool.allocate(3));
			assertEquals(Optional.empty(), pool.reallocate(3));
		}

		@DisplayName("can destroy the allocated blocks")
		@Test
    	void destroy() {
    		pool.destroy();
    		assertEquals(2, block.remaining());
			assertEquals(0, pool.size());
			assertEquals(0, pool.free());
			assertEquals(0, pool.blocks());
			assertEquals(0, pool.allocations().count());
    	}
	}

	@DisplayName("A pool with partially allocated memory blocks...")
	@Nested
	class Partial {
		private DeviceMemory memory;

		@BeforeEach
		void before() {
    		pool.add(new Block(allocator.allocate(MockAllocator.MEMORY_TYPE, 2)));
			memory = pool.allocate(1).get();
		}

		@DisplayName("can allocate from an available block with sufficient remaining memory")
		@Test
		void allocate() {
			final DeviceMemory remaining = pool.allocate(1).get();
			assertEquals(1, remaining.size());
			assertEquals(MockAllocator.MEMORY_TYPE, remaining.type());
			assertEquals(false, remaining.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(0, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(2, pool.allocations().count());
		}

		@DisplayName("cannot allocate if there are no blocks with sufficient free memory")
		@Test
		void none() {
			assertEquals(Optional.empty(), pool.allocate(3));
			assertEquals(Optional.empty(), pool.reallocate(3));
		}

		@DisplayName("can release the allocated memory back to the pool")
		@Test
    	void release() {
    		pool.release();
    		assertEquals(true, memory.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(0, pool.allocations().count());
    	}

		@DisplayName("can destroy all allocated blocks")
		@Test
    	void destroy() {
    		pool.destroy();
    		assertEquals(true, memory.isDestroyed());
			assertEquals(0, pool.size());
			assertEquals(0, pool.free());
			assertEquals(0, pool.blocks());
			assertEquals(0, pool.allocations().count());
    	}
	}

	@DisplayName("A pool with no free memory blocks...")
	@Nested
	class Exhausted {
		@BeforeEach
		void before() {
    		pool.add(new Block(allocator.allocate(MockAllocator.MEMORY_TYPE, 2)));
			pool.allocate(2).get();
		}

		@DisplayName("cannot allocate new memory")
		@Test
		void allocate() {
			assertEquals(Optional.empty(), pool.allocate(1));
			assertEquals(Optional.empty(), pool.reallocate(1));
		}
	}

	@DisplayName("A pool with a block containing released memory...")
	@Nested
	class Destroyed {
		private DeviceMemory memory;

		@BeforeEach
		void before() {
    		pool.add(new Block(allocator.allocate(MockAllocator.MEMORY_TYPE, 2)));
			memory = pool.allocate(2).get();
			memory.destroy();
		}

		@DisplayName("has available free memory that can be reallocated")
		@Test
		void free() {
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(0, pool.allocations().count());
		}

		@Disabled
		@DisplayName("can reallocate the released memory")
		@Test
		void reallocate() {
			assertEquals(Optional.of(memory), pool.reallocate(2));
			assertEquals(2, pool.size());
			assertEquals(0, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(1, pool.allocations().count());
		}

		@Disabled
		@DisplayName("can reallocate a portion of the released memory")
		@Test
		void portion() {
			final DeviceMemory portion = pool.reallocate(1).get();
			assertEquals(1, portion.size());
			assertEquals(2, pool.size());
			assertEquals(1, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(1, pool.allocations().count());
		}
	}
}
