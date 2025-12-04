package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.foreign.MemoryLayout;
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
		final var field = new StructureField<>("field", new NativeType("int", JAVA_INT));
		final MemoryLayout layout = builder.layout("VkStructure", GroupType.UNION, List.of(field));
		final var expected = MemoryLayout.unionLayout(JAVA_INT.withName("field"));
		assertEquals(expected, layout);
	}

	@DisplayName("Padding should be injected into a structure to align each fields to 8-bytes")
	@Test
	void padding() {
		final var one = new StructureField<>("one", new NativeType("int", JAVA_INT));
		final var two = new StructureField<>("two", TypeMapper.HANDLE);
		final var layout = builder.layout("VkStructure", GroupType.STRUCT, List.of(one, two));
		final var expected = MemoryLayout.structLayout(
				JAVA_INT.withName("one"),
				MemoryLayout.paddingLayout(4),
				ADDRESS.withName("two")
		);
		assertEquals(expected, layout);
	}

	@DisplayName("An existing structure can be nested in subsequent layouts")
	@Test
	void nested() {
		// Register a structure to be nested
		builder.layout("VkNested", GroupType.STRUCT, List.of());

		// Build layout for a structure with a nested field
		final var field = new StructureField<>("nested", new NativeType("VkNested", MemoryLayout.structLayout()));
		final MemoryLayout layout = builder.layout("VkStructure", GroupType.STRUCT, List.of(field));

		// Check nested layout
		final var expected = MemoryLayout.structLayout(
				MemoryLayout.structLayout().withName("nested")
		);
		assertEquals(expected, layout);
	}

	@DisplayName("Padding should be appended to a structure array to ensure the elements are aligned")
	@Test
	void arrays() {
		// Define a structure that is not aligned
		final var structure = MemoryLayout.structLayout(
				JAVA_LONG.withName("size"),
				JAVA_INT.withName("flags")
		);

		// Use structure in an array field which will require alignment
		final var field = new StructureField<>("pStructureArray", new NativeType("VkStructure", structure), 4);
		final MemoryLayout result = builder.layout("VkStructure", GroupType.STRUCT, List.of(field));

		// Check padding added to array field
		final MemoryLayout expected = MemoryLayout.structLayout(
				MemoryLayout.sequenceLayout(
						4,
						MemoryLayout.structLayout(
								JAVA_LONG.withName("size"),
								JAVA_INT.withName("flags"),
								MemoryLayout.paddingLayout(4)
						)
				).withName("pStructureArray")
		);
		assertEquals(expected, result);
	}
}
