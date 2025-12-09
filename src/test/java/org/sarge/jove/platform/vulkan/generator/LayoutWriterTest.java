package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.foreign.*;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LayoutWriterTest {
	private LayoutWriter writer;

	@BeforeEach
	void before() {
		writer = new LayoutWriter(1);
	}

	@Nested
	class Fields {
		@ParameterizedTest
		@MethodSource
		void value(ValueLayout layout) {
			final String expected = "JAVA_" + layout.carrier().getSimpleName().toUpperCase();
			assertEquals(expected, writer.write(layout));
		}

		@Test
		void pointer() {
			assertEquals("POINTER", writer.write(AddressLayout.ADDRESS));
		}

		private static ValueLayout[] value() {
			return new ValueLayout[] {
				JAVA_BOOLEAN,
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
			};
		}
	}

	@Nested
	class Padding {
		@Test
		void padding() {
			assertEquals("MemoryLayout.paddingLayout(3)", writer.write(MemoryLayout.paddingLayout(3)));
		}

		@Test
		void paddingConstant() {
			assertEquals("PADDING", writer.write(MemoryLayout.paddingLayout(4)));
		}
	}

	@Nested
	class ArrayTest {
		@DisplayName("A primitive array is rendered inline")
		@Test
		void sequencePrimitive() {
			final SequenceLayout sequence = MemoryLayout.sequenceLayout(4, JAVA_FLOAT);
			assertEquals("MemoryLayout.sequenceLayout(4, JAVA_FLOAT)", writer.write(sequence));
		}

		@DisplayName("A compound array is rendered recursively")
		@Test
		void sequenceCompound() {
			final String expected = """
					MemoryLayout.sequenceLayout(4, MemoryLayout.structLayout(
							JAVA_INT.withName("one"),
							JAVA_INT.withName("two")
						))""";

			final var structure = MemoryLayout.structLayout(
					JAVA_INT.withName("one"),
					JAVA_INT.withName("two")
			);

			final SequenceLayout sequence = MemoryLayout.sequenceLayout(4, structure);
			assertEquals(expected, writer.write(sequence));
		}
	}

	@Nested
	class StructureTest {
		@DisplayName("The fields of a structure are indented and suffixed with the member name")
		@Test
		void structure() {
			final String expected = """
					MemoryLayout.structLayout(
						JAVA_INT.withName("one"),
						JAVA_INT.withName("two")
					)""";

			final var structure = MemoryLayout.structLayout(
					JAVA_INT.withName("one"),
					JAVA_INT.withName("two")
			);

			assertEquals(expected, writer.write(structure));
		}

		@DisplayName("A structure with a single field is not indented")
		@Test
		void structureSingleField() {
			final String expected = """
					MemoryLayout.structLayout(JAVA_INT.withName("field"))""";

			final var structure = MemoryLayout.structLayout(JAVA_INT.withName("field"));
			assertEquals(expected, writer.write(structure));
		}

		@DisplayName("Structure padding does not have a name")
		@Test
		void structurePadding() {
			final var structure = MemoryLayout.structLayout(MemoryLayout.paddingLayout(4));
			assertEquals("MemoryLayout.structLayout(PADDING)", writer.write(structure));
		}

		@DisplayName("A nested structure is rendered recursively")
		@Test
		void structureNested() {
			final String expected = """
					MemoryLayout.structLayout(
						MemoryLayout.structLayout(
							JAVA_INT.withName("one"),
							JAVA_INT.withName("two")
						).withName("child")
					)""";

			final var child = MemoryLayout.structLayout(JAVA_INT.withName("one"), JAVA_INT.withName("two"));
			final var parent = MemoryLayout.structLayout(child.withName("child"));
			assertEquals(expected, writer.write(parent));
		}

		@DisplayName("A structure field must have a name")
		@Test
		void memberRequiresName() {
			final var structure = MemoryLayout.structLayout(JAVA_INT);
			assertThrows(NoSuchElementException.class, () -> writer.write(structure));
		}

		@DisplayName("A union is rendered similarly to a structure")
		@Test
		void union() {
			final String expected = """
					MemoryLayout.unionLayout(JAVA_INT.withName("field"))""";

			final var structure = MemoryLayout.unionLayout(JAVA_INT.withName("field"));
			assertEquals(expected, writer.write(structure));
		}
	}
}
