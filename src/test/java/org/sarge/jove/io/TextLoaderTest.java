package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;

@SuppressWarnings("unchecked")
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
		assertThrows(IOException.class, () -> loader.load(new StringReader("text"), consumer));
	}
}
