package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.MemoryLayout;

import org.junit.jupiter.api.*;

class FieldAlignmentTest {
	private FieldAlignment alignment;

	@BeforeEach
	void before() {
		alignment = new FieldAlignment();
	}

	@DisplayName("The accumulated byte alignment is initially zero")
	@Test
	void empty() {
		assertEquals(0, alignment.alignment());
		assertEquals(0, alignment.padding());
	}

	@DisplayName("The alignment word size must be a power-of-two")
	@Test
	void word() {
		assertThrows(IllegalArgumentException.class, () -> new FieldAlignment(0));
		assertThrows(IllegalArgumentException.class, () -> new FieldAlignment(3));
	}

	@Nested
	class Primitive {
		@Nested
		class AlignByte {
			@Test
			void align() {
				assertEquals(0L, alignment.align(JAVA_BYTE));
				assertEquals(1L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				for(long n = 0; n < 7; ++n) {
					assertEquals(0L, alignment.align(JAVA_BYTE));
					assertEquals(n + 1, alignment.alignment());
				}
				assertEquals(0L, alignment.align(JAVA_BYTE));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}

		@Nested
		class AlignShort {
			@Test
			void align() {
				assertEquals(0L, alignment.align(JAVA_SHORT));
				assertEquals(2L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_BYTE);
				assertEquals(1L, alignment.align(JAVA_SHORT));
				assertEquals(4L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}

		@Nested
		class AlignInteger {
			@Test
			void align() {
				assertEquals(0L, alignment.align(JAVA_INT));
				assertEquals(4L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_BYTE);
				assertEquals(3L, alignment.align(JAVA_INT));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void two() {
				assertEquals(0L, alignment.align(JAVA_INT));
				assertEquals(0L, alignment.align(JAVA_INT));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}

		@Nested
		class AlignLong {
			@Test
			void align() {
				assertEquals(0L, alignment.align(JAVA_LONG));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_INT);
				assertEquals(4L, alignment.align(JAVA_LONG));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}
	}

	@Nested
	class Types {
		@Nested
		class Address {
			@Test
			void align() {
				assertEquals(0L, alignment.align(ADDRESS));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_INT);
				assertEquals(4L, alignment.align(ADDRESS));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}

		@Nested
		class Sequence {
			private MemoryLayout sequence;

			@BeforeEach
			void before() {
				sequence = MemoryLayout.sequenceLayout(4, JAVA_INT);
			}

			@Test
			void align() {
				assertEquals(0L, alignment.align(sequence));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_SHORT);
				assertEquals(2L, alignment.align(sequence));
				assertEquals(4L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}

		@Nested
		class Structure {
			private MemoryLayout structure;

			@BeforeEach
			void before() {
				structure = MemoryLayout.structLayout(JAVA_INT, JAVA_INT, JAVA_INT);
			}

			@Test
			void align() {
				assertEquals(0L, alignment.align(structure));
				assertEquals(4L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}

			@Test
			void padding() {
				alignment.align(JAVA_SHORT);
				assertEquals(2L, alignment.align(structure));
				assertEquals(0L, alignment.alignment());
				assertEquals(0L, alignment.padding());
			}
		}
	}
}
