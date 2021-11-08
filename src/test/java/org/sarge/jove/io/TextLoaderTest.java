package org.sarge.jove.io;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TextLoaderTest {
	private TextLoader loader;
	private Function<Stream<String>, List<String>> mapper;

	@BeforeEach
	void before() {
		loader = new TextLoader();
		mapper = stream -> stream.collect(toList());
	}

	@Test
	void load() throws IOException {
		// Create text
		final String text =
				"""
				header
				- comment

				line
				""";

		// Init loader
		loader.setSkipHeaderLines(1);
		loader.setComments(Set.of("-"));

		// Load lines
		final List<String> list = loader.load(new StringReader(text), mapper);
		assertEquals(List.of("line"), list);
	}

	@Test
	void error() {
		mapper = stream -> {
			throw new IllegalArgumentException("Whatever");
		};
		assertThrows(IOException.class, "Whatever at line 1", () -> loader.load(new StringReader("text"), mapper));
	}
}
