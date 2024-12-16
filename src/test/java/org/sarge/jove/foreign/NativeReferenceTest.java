package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeReference.NativeReferenceTransformer;
import org.sarge.jove.foreign.NativeTransformer.ParameterMode;

class NativeReferenceTest {
	private NativeReferenceTransformer transformer;
	private NativeReference.Factory factory;
	private Arena arena;

	@BeforeEach
	void before() {
		factory = new NativeReference.Factory();
		transformer = new NativeReferenceTransformer();
		arena = Arena.ofAuto();
	}

	@DisplayName("A native reference cannot be null")
	@Test
	void empty() {
		assertThrows(NullPointerException.class, () -> transformer.transform(null, ParameterMode.REFERENCE, arena));
	}

	@DisplayName("A native reference cannot be returned from a native method")
	@Test
	void returns() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.returns());
	}

	@Nested
	class IntegerReference {
		private NativeReference<Integer> integer;

		@BeforeEach
		void before() {
			integer = factory.integer();
		}

		@DisplayName("An integer reference is zero until it has been returned from a native method")
		@Test
		void empty() {
			assertEquals(0, integer.get());
		}

		@DisplayName("An integer reference is transformed to a pointer to an integer")
		@Test
		void transform() {
			final MemorySegment address = transformer.transform(integer, ParameterMode.REFERENCE, arena);
			address.set(ValueLayout.JAVA_INT, 0, 42);
		}

		@DisplayName("An integer reference is returned as a by-reference parameter")
		@Test
		void update() {
			final MemorySegment address = transformer.transform(integer, ParameterMode.REFERENCE, arena);
			address.set(ValueLayout.JAVA_INT, 0, 42);
			transformer.update().accept(address, integer);
			assertEquals(42, integer.get());
		}
	}

	@Nested
	class PointerReference {
		private NativeReference<Handle> pointer;

		@BeforeEach
		void before() {
			pointer = factory.pointer();
		}

		@DisplayName("A pointer reference is null until it has been returned from a native method")
		@Test
		void empty() {
			assertEquals(null, pointer.get());
		}

		@DisplayName("A pointer reference is transformed to a native pointer")
		@Test
		void transform() {
			final MemorySegment address = transformer.transform(pointer, ParameterMode.REFERENCE, arena);
			final MemorySegment segment = MemorySegment.ofAddress(42);
			address.set(ADDRESS, 0, segment);
		}

		@DisplayName("A pointer reference is returned as a by-reference parameter")
		@Test
		void update() {
			final MemorySegment address = transformer.transform(pointer, ParameterMode.REFERENCE, arena);
			final MemorySegment segment = MemorySegment.ofAddress(42);
			address.set(ADDRESS, 0, segment);
			transformer.update().accept(address, pointer);
			assertEquals(new Handle(segment), pointer.get());
		}
	}
}
