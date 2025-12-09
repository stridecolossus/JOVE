package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.generator.StructureData.GroupType;

class StructureParserTest {
	@Test
	void parse() {
		// Define a structure
		final String source = """
				typedef struct VkStructure {
					VkStructureType			sType;
					uint32_t				integer;
					char					pCharArray[LENGTH];
					float					colour[4];
					VkStructure				nested;
					const VkStructure*		pStructure;
					const void*				pVoid;
					const char* const*		pStringArray;
				} VkStructure;
				""";

		// Init tokenizer
		final var tokenizer = new Tokenizer(source);
		tokenizer.skip("typedef");
		tokenizer.skip("struct");

		// Parse structure to metadata
		final var parser = new StructureParser(tokenizer, _ -> 3);
		final StructureData structure = parser.parse("struct");

		// Check against expected metadata
		@SuppressWarnings("rawtypes")
		final StructureField[] fields = {
				new StructureField<>("sType",			"VkStructureType",	0),
				new StructureField<>("integer",			"uint32_t",			0),
				new StructureField<>("pCharArray",		"char",				3),
				new StructureField<>("colour",			"float",			4),
				new StructureField<>("nested",			"VkStructure",		0),
				new StructureField<>("pStructure",		"VkStructure*",		0),
				new StructureField<>("pVoid",			"void*",			0),
				new StructureField<>("pStringArray",	"char**",			0),
		};
		@SuppressWarnings("unchecked")
		final var expected = new StructureData("VkStructure", GroupType.STRUCT, List.of(fields));
		assertEquals(expected, structure);
	}
}
