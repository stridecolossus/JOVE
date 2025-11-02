package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;

import org.junit.jupiter.api.*;

class ArrayTransformerTest {
	private SegmentAllocator allocator;

	@BeforeEach
	void before() {
		allocator = Arena.ofAuto();
	}

//	@Nested
//	class PrimitiveArray {
//		private ArrayTransformer transformer;
//
//		@BeforeEach
//		void before() {
//			transformer = new ArrayTransformer(new IdentityTransformer<>(JAVA_INT));
//		}
//
//		@Test
//		void layout() {
//			assertEquals(ADDRESS, transformer.layout());
//		}
//
//		@Test
//		void marshal() {
//			final MemorySegment address = transformer.marshal(new int[]{2, 3}, allocator);
//			assertEquals(2 * 4, address.byteSize());
//			assertEquals(2, address.getAtIndex(JAVA_INT, 0L));
//			assertEquals(3, address.getAtIndex(JAVA_INT, 1L));
//		}
//
//		@Test
//		void unmarshal() {
//			assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
//		}
//
//		@Test
//		void update() {
//			final MemorySegment address = allocator.allocate(JAVA_INT, 3);
//			address.setAtIndex(JAVA_INT, 0L, 2);
//			address.setAtIndex(JAVA_INT, 1L, 3);
//
//			final int[] array = new int[2];
//			transformer.update().accept(address, array);
//			assertArrayEquals(new int[]{2, 3}, array);
//		}
//	}

	@Nested
	class ReferenceArray {
		private ArrayTransformer transformer;

		@BeforeEach
		void before() {
			transformer = new ArrayTransformer(new StringTransformer());
		}

		@Test
		void layout() {
			assertEquals(ADDRESS, transformer.layout());
		}

		@Test
		void marshal() {
			final String string = "string";
			final MemorySegment address = transformer.marshal(new String[]{string}, allocator);
			final MemorySegment element = address.getAtIndex(ADDRESS, 0);
			assertEquals(string, element.reinterpret(Integer.MAX_VALUE).getString(0L));
		}

		@Test
		void unmarshal() {
			assertThrows(UnsupportedOperationException.class, () -> transformer.unmarshal());
		}

		@Test
		void update() {
			final String string = "string";
			final MemorySegment address = allocator.allocate(ADDRESS, 1);
			address.setAtIndex(ADDRESS, 0L, allocator.allocateFrom(string));

			final String[] array = new String[1];
			transformer.update().accept(address, array);
			assertEquals(string, array[0]);
		}

		@Test
		void empty() {
			final MemorySegment address = allocator.allocate(ADDRESS, 1);
			final String[] array = new String[1];
			transformer.update().accept(address, array);
			assertEquals(null, array[0]);
		}
	}
}
