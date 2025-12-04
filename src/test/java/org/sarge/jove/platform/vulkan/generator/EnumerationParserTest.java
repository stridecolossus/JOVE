package org.sarge.jove.platform.vulkan.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.*;

class EnumerationParserTest {
	private EnumerationParser parser;

	@BeforeEach
	void before() {
		parser = new EnumerationParser();
	}

	@Test
	void parse() {
		// Define an enumeration (with a synonym)
		final String source = """
				typedef enum VkFilter {
				    VK_FILTER_NEAREST = 0,
				    VK_FILTER_LINEAR = 1,
				    VK_FILTER_CUBIC_IMG = 1000015000,
				    VK_FILTER_CUBIC_EXT = VK_FILTER_CUBIC_IMG,
				    VK_FILTER_MAX_ENUM = 0x7FFFFFFF
				} VkFilter;
				""";

		// Build expected enumeration metadata (values are ordered)
		final var values = new LinkedHashMap<String, Integer>();
		values.put("VK_FILTER_NEAREST", 0);
		values.put("VK_FILTER_LINEAR", 1);
		values.put("VK_FILTER_CUBIC_IMG", 1000015000);
		values.put("VK_FILTER_CUBIC_EXT", 1000015000);
		values.put("VK_FILTER_MAX_ENUM", Integer.MAX_VALUE);
		final var expected = new EnumerationData("VkFilter", values);

		// Init tokens (skipping those already parsed)
		final var tokenizer = new Tokenizer(source);
		tokenizer.skip("typedef");
		tokenizer.skip("enum");

		// Parse enumeration
		final EnumerationData enumeration = parser.parse(tokenizer);
		assertEquals(expected, enumeration);
	}
}
