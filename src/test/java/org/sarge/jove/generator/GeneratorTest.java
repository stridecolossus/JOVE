package org.sarge.jove.generator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GeneratorTest {
	private Generator generator;
	private TemplateProcessor proc;

	@BeforeEach
	public void before() {
		proc = mock(TemplateProcessor.class);
		generator = new Generator(proc, "template", "package");
	}

	@Test
	public void generate() {
		final Map<String, Object> values = Map.of("field", "value");
		final Map<String, Object> expected = Map.of(
			"name", "name",
			"package", "package",
			"field", "value"
		);
		generator.generate("name", values);
		verify(proc).generate("template", expected);
	}
}
