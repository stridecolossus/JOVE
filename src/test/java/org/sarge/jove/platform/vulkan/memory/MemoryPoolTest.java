package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MemoryPoolTest {
	private MemoryPool pool;
	private MemoryType type;
	private Block block;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		type = new MemoryType(0, new Heap(0, Set.of()), Set.of());
		block = new Block(new DefaultDeviceMemory(new Handle(1), dev, type, 2));
		pool = new MemoryPool(type);
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
    		pool.add(block);
    		assertEquals(2, pool.size());
    		assertEquals(2, pool.free());
    		assertEquals(1, pool.blocks());
    		assertEquals(0, pool.allocations().count());
    	}

    	@DisplayName("cannot add a block that is in use")
    	@Test
    	void used() {
    		block.allocate(1);
    		assertThrows(IllegalArgumentException.class, () -> pool.add(block));
    	}
	}

	@DisplayName("A pool with a free memory block...")
	@Nested
	class Free {
		@BeforeEach
		void before() {
			pool.add(block);
		}

		@DisplayName("can allocate from the block")
		@Test
		void allocate() {
			final DeviceMemory mem = pool.allocate(1).get();
			assertEquals(1, mem.size());
			assertEquals(type, mem.type());
			assertEquals(false, mem.isDestroyed());
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

		@DisplayName("cannot add the same block more than once")
		@Test
		void duplicate() {
			assertThrows(IllegalArgumentException.class, () -> pool.add(block));
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
		private DeviceMemory mem;

		@BeforeEach
		void before() {
			pool.add(block);
			mem = pool.allocate(1).get();
		}

		@DisplayName("can allocate from an available block with sufficient remaining memory")
		@Test
		void allocate() {
			final DeviceMemory remaining = pool.allocate(1).get();
			assertEquals(1, remaining.size());
			assertEquals(type, remaining.type());
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
    		assertEquals(true, mem.isDestroyed());
			assertEquals(2, pool.size());
			assertEquals(2, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(0, pool.allocations().count());
    	}

		@DisplayName("can destroy all allocated blocks")
		@Test
    	void destroy() {
    		pool.destroy();
    		assertEquals(true, mem.isDestroyed());
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
			pool.add(block);
			pool.allocate(2).get();
		}

		@DisplayName("cannot allocate new memory")
		@Test
		void allocate() {
			assertEquals(Optional.empty(), pool.allocate(1));
			assertEquals(Optional.empty(), pool.reallocate(1));
		}
	}

	@DisplayName("A pool with a block containing destroyed memory...")
	@Nested
	class Destroyed {
		private DeviceMemory mem;

		@BeforeEach
		void before() {
			pool.add(block);
			mem = pool.allocate(2).get();
			mem.destroy();
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
		@DisplayName("can reallocate the destroyed memory")
		@Test
		void reallocate() {
			assertEquals(Optional.of(mem), pool.reallocate(2));
			assertEquals(2, pool.size());
			assertEquals(0, pool.free());
			assertEquals(1, pool.blocks());
			assertEquals(1, pool.allocations().count());
		}

		@Disabled
		@DisplayName("can reallocate a portion of the destroyed memory")
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
