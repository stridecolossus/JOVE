package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.generator.StructureData.GroupType;

class LayoutBuilderTest {
	private LayoutBuilder builder;

	@BeforeEach
	void before() {
		builder = new LayoutBuilder();
	}

	@DisplayName("The memory layout of a structure can be constructed from its fields")
	@Test
	void layout() {
		final var one = new StructureField<>("one", NativeType.of(JAVA_INT));
		final var two = new StructureField<>("two", NativeType.of(JAVA_INT));
		final MemoryLayout layout = builder.layout("VkStructure", GroupType.UNION, List.of(one, two));
		final var expected = MemoryLayout.unionLayout(
				JAVA_INT.withName("one"),
				JAVA_INT.withName("two")
		);
		assertEquals(expected, layout);
	}

	@DisplayName("Padding should be injected into a structure to align each field to a word boundary")
	@Test
	void padding() {
		final var one = new StructureField<>("one", new NativeType("int", JAVA_INT));
		final var two = new StructureField<>("two", new NativeType("pointer", ADDRESS));
		final var layout = builder.layout("VkStructure", GroupType.STRUCT, List.of(one, two));
		final var expected = MemoryLayout.structLayout(
				JAVA_INT.withName("one"),
				MemoryLayout.paddingLayout(4),
				ADDRESS.withName("two")
		);
		assertEquals(expected, layout);
	}

	@DisplayName("Padding should be appended to the end of a structure to align to the largest type")
	@Test
	void append() {
		final var one = new StructureField<>("one", new NativeType("long", JAVA_LONG));
		final var two = new StructureField<>("two", new NativeType("int", JAVA_INT));
		final var layout = builder.layout("VkStructure", GroupType.STRUCT, List.of(one, two));
		final var expected = MemoryLayout.structLayout(
				JAVA_LONG.withName("one"),
				JAVA_INT.withName("two"),
				MemoryLayout.paddingLayout(4)
		);
		assertEquals(expected, layout);
	}

	@DisplayName("An existing structure can be nested in subsequent layouts")
	@Test
	void nested() {
		// Define a nested structure
		final var nested = MemoryLayout.structLayout(ValueLayout.JAVA_INT.withName("field"));

		// Build layout for a structure with a nested field
		final var field = new StructureField<>("nested", new NativeType("VkNested", nested));
		final MemoryLayout layout = builder.layout("VkStructure", GroupType.STRUCT, List.of(field));

		// Check nested layout
		final var expected = MemoryLayout.structLayout(
				MemoryLayout.structLayout(JAVA_INT.withName("field")).withName("nested")
		);
		assertEquals(expected, layout);
	}
}
