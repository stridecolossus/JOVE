package org.sarge.jove.lib;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class PointerTest {
	private Pointer pointer;

	@BeforeEach
	void before() {
		pointer = new Pointer();
	}

	@Nested
	class Unallocated {
		@Test
		void isAllocated() {
			assertEquals(false, pointer.isAllocated());
		}

		@Test
		void address() {
			assertEquals(null, pointer.address());
		}

		@Test
		void allocate() {
			pointer.allocate(ADDRESS, new NativeContext());
		}
	}

	@Nested
	class Allocated {
		@BeforeEach
		void before() {
			pointer.allocate(ADDRESS, new NativeContext());
		}

		@Test
		void isAllocated() {
			assertEquals(true, pointer.isAllocated());
		}

		@Test
		void pointer() {
			assertNotNull(pointer.address());
		}

		@Test
		void allocate() {
			assertSame(pointer.address(), pointer.allocate(ADDRESS, new NativeContext()));
		}
	}
}
