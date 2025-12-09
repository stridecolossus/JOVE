package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import org.junit.jupiter.api.Test;

class EnumerationGeneratorTest {
	@Test
	void generate() {
		// Build enumeration (partial)
		final var values = new LinkedHashMap<String, Integer>();
	    values.put("VK_ACCESS_INDIRECT_COMMAND_READ_BIT", 0x00000001);
		values.put("VK_ACCESS_INDEX_READ_BIT", 0x00000002);
		values.put("VK_ACCESS_TRANSFORM_FEEDBACK_WRITE_BIT_EXT", 0x02000000);
		values.put("VK_ACCESS_FLAG_BITS_MAX_ENUM", 0x7FFFFFFF);

		// Generate template arguments
		final var enumeration = new EnumerationData("VkAccessFlagBits", values);
		final var generator = new EnumerationGenerator();
		final var arguments = generator.generate(enumeration);

		// Compare against expected arguments
		final var list = List.of(
				Map.entry("INDIRECT_COMMAND_READ", 1),
				Map.entry("INDEX_READ", 2),
				Map.entry("TRANSFORM_FEEDBACK_WRITE_EXT", 33554432),
				Map.entry("FLAG_BITS_MAX_ENUM", 2147483647)
		);
		final Map<String, Object> expected = Map.of(
				"name", "VkAccessFlags",
				"values", list
		);
		assertEquals(expected, arguments);
	}
}
