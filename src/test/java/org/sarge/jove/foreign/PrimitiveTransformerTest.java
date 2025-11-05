package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sarge.jove.foreign.PrimitiveTransformer.NativeBooleanTransformer;

class PrimitiveTransformerTest {
	private PrimitiveTransformer<Integer> transformer;

	@BeforeEach
	void before() {
		transformer = new PrimitiveTransformer<>(JAVA_INT);
	}

	@Test
	void layout() {
		assertEquals(JAVA_INT, transformer.layout());
	}

	@Test
	void marshal() {
		assertEquals(3, transformer.marshal(3, null));
	}

	@Test
	void empty() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.empty());
	}

	@Test
	void unmarshal() {
		assertEquals(4, transformer.unmarshal().apply(4));
	}

	@Test
	void update() {
		assertThrows(UnsupportedOperationException.class, () -> transformer.update());
	}

	@Nested
	class NativeBooleanTransformerTest {
		private NativeBooleanTransformer bool;

		@BeforeEach
		void before() {
			bool = new NativeBooleanTransformer();
		}

		@Test
		void layout() {
			assertEquals(JAVA_INT, bool.layout());
		}

		@Test
		void marshal() {
			assertEquals(1, bool.marshal(true, null));
			assertEquals(0, bool.marshal(false, null));
		}

		@Test
		void empty() {
			assertThrows(UnsupportedOperationException.class, () -> bool.empty());
		}

		@Test
		void unmarshal() {
			assertEquals(true, bool.unmarshal().apply(1));
			assertEquals(false, bool.unmarshal().apply(0));
		}

		@Test
		void update() {
			assertThrows(UnsupportedOperationException.class, () -> bool.update());
		}
	}

	@Nested
	class ArrayTest {
		@SuppressWarnings("rawtypes")
		private Transformer arrayTransformer;
		private SegmentAllocator allocator;

		@BeforeEach
		void before() {
			arrayTransformer = transformer.array();
			allocator = Arena.ofAuto();
		}

		@Test
		void layout() {
			assertEquals(ValueLayout.ADDRESS, arrayTransformer.layout());
		}

		@Test
		void empty() {
			assertEquals(MemorySegment.NULL, arrayTransformer.empty());
		}

		@SuppressWarnings("unchecked")
		@Test
		void marshal() {
			final var address = (MemorySegment) arrayTransformer.marshal(new int[]{2, 3}, allocator);
			assertEquals(2 * 4, address.byteSize());
			assertEquals(2, address.getAtIndex(JAVA_INT, 0L));
			assertEquals(3, address.getAtIndex(JAVA_INT, 1L));
		}

		@Test
		void unmarshal() {
			assertThrows(UnsupportedOperationException.class, () -> arrayTransformer.unmarshal());
		}

		@SuppressWarnings("unchecked")
		@Test
		void update() {
			final MemorySegment address = allocator.allocate(JAVA_INT, 3);
			address.setAtIndex(JAVA_INT, 0L, 2);
			address.setAtIndex(JAVA_INT, 1L, 3);

			final int[] array = new int[2];
			arrayTransformer.update().accept(address, array);
			assertArrayEquals(new int[]{2, 3}, array);
		}
	}

	@Nested
	class RegistryTest {
    	private Registry registry;

    	@BeforeEach
    	void before() {
    		registry = new Registry();
    		PrimitiveTransformer.register(registry);
    	}

    	static ValueLayout[] primitives() {
    		return new ValueLayout[] {
    				JAVA_BYTE,
    				JAVA_CHAR,
    				JAVA_SHORT,
    				JAVA_INT,
    				JAVA_LONG,
    				JAVA_FLOAT,
    				JAVA_DOUBLE,
    		};
    	}

    	@ParameterizedTest
    	@MethodSource
    	void primitives(ValueLayout layout) {
			final var transformer = registry.transformer(layout.carrier()).orElseThrow();
    		assertEquals(layout, transformer.layout());
    	}

    	@Test
    	void bool() {
    		final var transformer = registry.transformer(boolean.class).orElseThrow();
    		assertEquals(JAVA_INT, transformer.layout());
    	}
    }
}
