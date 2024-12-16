package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Handle.HandleNativeTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class ArrayNativeTransformerTest {
	private ArrayNativeTransformer transformer;
	private Handle[] array;
	private Arena arena;

	// TODO
	// - check structure[] and primitive[]
	// - check by-reference variants

	@BeforeEach
	void before() {
		final var registry = new TransformerRegistry();
		registry.add(new HandleNativeTransformer());
		arena = Arena.ofAuto();
		array = new Handle[1];
		transformer = new ArrayNativeTransformer(registry);
	}

	@Test
	void constructor() {
		assertEquals(ADDRESS, transformer.layout(Object[].class));
		assertEquals(Handle[].class, transformer.type());
	}

	@Test
	void transform() {
		final Handle handle = new Handle(42);
		array[0] = handle;

		final MemorySegment address = transformer.transform(array, ParameterMode.VALUE, null);
		assertEquals(handle.address(), address.getAtIndex(ADDRESS, 0));
	}

	@Test
	void empty() {
		assertEquals(MemorySegment.NULL, transformer.empty(Object[].class));
	}

	@Test
	void transformNullElement() {
		final MemorySegment address = transformer.transform(array, ParameterMode.VALUE, null);
		assertEquals(MemorySegment.NULL, address.getAtIndex(ADDRESS, 0));
	}

	@Test
	void returns() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.returns(Object[].class));
	}

	private MemorySegment create() {
		final MemorySegment element = MemorySegment.ofAddress(42);
		final MemorySegment address = arena.allocate(ADDRESS, 1);
		address.setAtIndex(ADDRESS, 0, element);
		return address;
	}

	@Test
	void update() {
		final MemorySegment address = create();
		transformer.update().accept(address, array);
		assertEquals(new Handle(42), array[0]);
	}

	// TODO
	@Test
	void updateNullElement() {
//		final MemorySegment address = create();
//		transformer.update().accept(address, array);
//		assertEquals(new Handle(42), array[0]);
	}
}
