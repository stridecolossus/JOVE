package org.sarge.jove.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TemplateProcessorTest {
	private TemplateProcessor proc;

	@BeforeEach
	public void before() {
		proc = new TemplateProcessor("src/test/resources");
	}

	@Test
	public void generate() throws IOException {
		// Build enumeration values
		final Map<String, Long> values = new LinkedHashMap<>();
		values.put("ONE", 1L);
		values.put("TWO", 2L);

		// Build template properties
		final Map<String, Object> data = Map.of(
			"package", "org.sarge.test",
			"name", "TestEnumeration",
			"values", values
		);

		// Process template
		final String expected = Files.readString(Paths.get("src/test/resources/TestEnumeration.java"));
		final String result = proc.generate("TestEnumeration.template.java", data);
		assertEquals(expected, result);
	}
}
