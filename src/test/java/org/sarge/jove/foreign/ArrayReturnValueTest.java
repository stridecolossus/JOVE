package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class ArrayReturnValueTest {
	private TransformerRegistry registry;
	private Arena arena;

	@BeforeEach
	void before() {
		registry = TransformerRegistry.create();
		arena = Arena.ofAuto();
	}

	@DisplayName("The resultant array can be retrieved from a native array return value once the actual length is available")
	@Test
	void array() {
		final MemorySegment address = arena.allocate(ADDRESS, 2);
		final String[] expected = {"one", "two"};
		address.setAtIndex(ADDRESS, 0, arena.allocateFrom(expected[0]));
		address.setAtIndex(ADDRESS, 1, arena.allocateFrom(expected[1]));

		final var value = new ArrayReturnValue<String>(address, registry);
		final String[] array = value.array(2, String[]::new);
		assertArrayEquals(expected, array);
	}
}
