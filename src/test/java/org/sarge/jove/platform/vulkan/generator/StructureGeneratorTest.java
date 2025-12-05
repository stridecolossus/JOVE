package org.sarge.jove.platform.vulkan.generator;

import static java.lang.foreign.ValueLayout.JAVA_INT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.platform.vulkan.generator.StructureData.GroupType.STRUCT;

import java.lang.foreign.MemoryLayout;
import java.util.*;

import org.junit.jupiter.api.*;

class StructureGeneratorTest {
	private StructureGenerator generator;
	private TypeMapper mapper;

	@BeforeEach
	void before() {
		mapper = new TypeMapper();
		generator = new StructureGenerator(mapper);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	void generate() {
		// Create structure metadata
		final StructureField[] fields = {
				new StructureField("sType",			"VkStructureType",	0),
				new StructureField("pNext",			"void*",			0),
				new StructureField("integer",		"uint32_t",			0),
				new StructureField("pCharArray",	"char",				4),
				new StructureField("colour",		"float",			4),
				new StructureField("nested",		"VkNested",			0),
				new StructureField("pStringArray",	"char**",			0),
		};
		final var structure = new StructureData("VkMockStructure", STRUCT, List.of(fields));

		// Register dependant generated types
		mapper.add("VkStructureType", new NativeType("VkStructureType", JAVA_INT));
		mapper.add("VkNested", new NativeType("VkNested", MemoryLayout.structLayout(JAVA_INT.withName("field"))));

		// Generate source code
		final Map<String, Object> arguments = generator.generate(structure);
		final var template = new TemplateProcessor();
		final String source = template.generate("structure-template.txt", arguments);

		// Check against expected source code
		final String expected = """
				package org.sarge.jove.platform.vulkan;

				import static java.lang.foreign.ValueLayout.*;

				import java.lang.foreign.*;

				import org.sarge.jove.foreign.NativeStructure;
				import org.sarge.jove.common.Handle;
				import org.sarge.jove.util.EnumMask;
				import org.sarge.jove.platform.vulkan.*;

				/**
				 * Vulkan structure.
				 * This class has been code-generated.
				 */
				public class VkMockStructure implements NativeStructure {
					public VkStructureType sType;
					public Handle pNext;
					public int integer;
					public String pCharArray;
					public float[] colour;
					public VkNested nested;
					public String[] pStringArray;

					@Override
					public GroupLayout layout() {
						return MemoryLayout.structLayout(
							JAVA_INT.withName("sType"),
							PADDING,
							POINTER.withName("pNext"),
							JAVA_INT.withName("integer"),
							MemoryLayout.sequenceLayout(4, JAVA_CHAR).withName("pCharArray"),
							MemoryLayout.sequenceLayout(4, JAVA_FLOAT).withName("colour"),
							MemoryLayout.structLayout(JAVA_INT.withName("field")).withName("nested"),
							POINTER.withName("pStringArray")
						);
					}
				}
				""";

		assertEquals(expected, source);
	}
}
