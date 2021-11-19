package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TextLoaderTest {
	private TextLoader loader;
	private Consumer<String> consumer;

	@BeforeEach
	void before() {
		loader = new TextLoader();
		consumer = mock(Consumer.class);
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
		final Consumer<String> consumer = mock(Consumer.class);
		loader.load(new StringReader(text), consumer);
		verify(consumer).accept("line");
		verifyNoMoreInteractions(consumer);
	}

	@Test
	void error() {
		doThrow(new IllegalArgumentException("Whatever")).when(consumer).accept(anyString());
		assertThrows(IOException.class, "Whatever at line 1", () -> loader.load(new StringReader("text"), consumer));
	}

	@Test
	void tokenize() {
		assertArrayEquals(new String[]{"1", "2", "3"}, TextLoader.tokenize("1  2\t 3 "));
	}
}
