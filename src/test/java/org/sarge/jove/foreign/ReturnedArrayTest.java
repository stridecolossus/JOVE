package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.BeforeEach;

class ReturnedArrayTest {
	private Arena arena;
	private Registry registry;

	@BeforeEach
	void before() {
		arena = Arena.ofAuto();
		registry = new Registry();
	}

//	@Test
	void get() {
		// Create an off-heap array
		final MemorySegment address = arena.allocate(ADDRESS, 2);
		address.setAtIndex(ADDRESS, 0, arena.allocateFrom("one"));
		address.setAtIndex(ADDRESS, 1, arena.allocateFrom("two"));

		// Register component type
		registry.register(String.class, new StringTransformer());

		// Extract array
		final var array = new ReturnedArray<String>(address, registry);
		final String[] expected = {"one", "two"};
		assertArrayEquals(expected, array.get(2, String.class));
	}

//	@Test
	void unsupported() {
		final MemorySegment address = arena.allocate(ADDRESS, 1);
		final var array = new ReturnedArray<String>(address, registry);
		assertThrows(IllegalArgumentException.class, () -> array.get(1, String.class));
	}
}
