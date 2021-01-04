package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.sarge.jove.common.DeviceMemory.Pool;
import org.sarge.jove.common.DeviceMemory.Pool.AllocationException;
import org.sarge.jove.common.DeviceMemory.Pool.Allocator;
import org.sarge.jove.common.NativeObject.Handle;

import com.sun.jna.Pointer;

public class DeviceMemoryTest {
	private Pool pool;
	private Allocator allocator;
	private Handle handle;

	@BeforeEach
	void before() {
		// Create memory block
		handle = new Handle(new Pointer(42));

		// Create allocator
		final Answer<DeviceMemory> answer = inv -> {
			final long size = inv.getArgument(0);
			final DeviceMemory block = mock(DeviceMemory.class);
			when(block.handle()).thenReturn(handle);
			when(block.size()).thenReturn(size);
			return block;
		};
		allocator = mock(Allocator.class);
		when(allocator.allocate(anyLong())).thenAnswer(answer);

		// Create pool
		pool = new Pool(allocator);
	}

	@Test
	void constructor() {
		assertEquals(0, pool.size());
		assertEquals(0, pool.free());
		assertEquals(0, pool.count());
		assertNotNull(pool.allocations());
		assertEquals(0, pool.allocations().count());
	}

	@Test
	void allocate() {
		// Allocate memory
		final DeviceMemory mem = pool.allocate(1);
		assertNotNull(mem);
		assertEquals(handle, mem.handle());
		assertEquals(0, mem.offset());
		assertEquals(1, mem.size());
		assertEquals(false, mem.isDestroyed());

		// Check new block allocated
		verify(allocator).allocate(1);

		// Check allocations
		assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.count());
	}

	@Test
	void allocateMultiple() {
		pool.add(10);
		int offset = 0;
		for(int n = 0; n < 3; ++n) {
			final DeviceMemory mem = pool.allocate(n + 1);
			assertNotNull(mem);
			assertEquals(n + 1, mem.size());
			assertEquals(offset, mem.offset());
			offset += mem.size();
		}
		assertEquals(10, pool.size());
		assertEquals(10 - (1 + 2 + 3), pool.free());
		assertEquals(3, pool.count());
		assertEquals(3, pool.allocations().count());
	}

	@Test
	void reallocate() {
		final DeviceMemory mem = pool.allocate(1);
		mem.destroy();
		assertEquals(mem, pool.allocate(1));
		assertEquals(false, mem.isDestroyed());
		assertEquals(1, pool.size());
		assertEquals(0, pool.free());
		assertEquals(1, pool.count());
		assertArrayEquals(new DeviceMemory[]{mem}, pool.allocations().toArray());
	}

	@Test
	void allocateZeroLength() {
		assertThrows(IllegalArgumentException.class, () -> pool.allocate(0));
	}

	@Test
	void allocateFailed() {
		when(allocator.allocate(anyLong())).thenThrow(new AllocationException("doh"));
		assertThrows(AllocationException.class, "doh", () -> pool.allocate(1));
	}

	@Test
	void allocateNullMemory() {
		when(allocator.allocate(anyLong())).thenReturn(null);
		assertThrows(AllocationException.class, "Allocator returned null memory", () -> pool.allocate(1));
	}

	@Test
	void destroy() {
		final DeviceMemory mem = pool.allocate(1);
		mem.destroy();
		assertEquals(true, mem.isDestroyed());
		assertEquals(0, pool.allocations().count());
		assertEquals(1, pool.size());
		assertEquals(1, pool.free());
		assertEquals(0, pool.count());
	}

	@Test
	void destroyAlreadyDestroyed() {
		final DeviceMemory mem = pool.allocate(1);
		mem.destroy();
		assertThrows(IllegalStateException.class, () -> mem.destroy());
	}

	@Test
	void add() {
		// Init pool
		pool.add(2);
		assertEquals(2, pool.size());
		assertEquals(2, pool.free());
		assertEquals(0, pool.count());
		clearInvocations(allocator);

		// Allocate and check memory is pre-allocated
		pool.allocate(1);
		verifyNoMoreInteractions(allocator);
		assertEquals(1, pool.free());
		assertEquals(1, pool.count());
	}

	@Test
	void addZeroSize() {
		assertThrows(IllegalArgumentException.class, () -> pool.add(0));
	}

	@Test
	void paged() {
		final Allocator paged = Allocator.paged(3, allocator);
		final DeviceMemory mem = paged.allocate(2);
		assertNotNull(mem);
		assertEquals(3, mem.size());
	}
}
