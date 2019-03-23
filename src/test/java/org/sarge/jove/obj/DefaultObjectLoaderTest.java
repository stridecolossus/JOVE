package org.sarge.jove.obj;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.obj.DefaultObjectLoader.ArrayParser;
import org.sarge.jove.obj.DefaultObjectLoader.Parser;

public class DefaultObjectLoaderTest {
	private static final String COMMAND = "cmd";

	private DefaultObjectLoader<Object> loader;
	private Parser<Object> parser;
	private Object model;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		loader = new DefaultObjectLoader<>();
		parser = mock(Parser.class);
		model = new Object();
	}

	@Test
	public void add() {
		loader.add(COMMAND, parser);
	}

	@Test
	public void addInvalidMinMax() {
		when(parser.min()).thenReturn(2);
		when(parser.max()).thenReturn(1);
		assertThrows(IllegalArgumentException.class, () -> loader.add(COMMAND, parser));
	}

	@Test
	public void loadEmptyLine() throws IOException {
		loader.load(new StringReader(""), model);
	}

	@Test
	public void loadCommentLine() throws IOException {
		loader.load(new StringReader("# comment"), model);
	}

	@Test
	public void loadUnsupportedCommand() {
		assertThrows(IOException.class, () -> loader.load(new StringReader(COMMAND), model));
	}

	@Test
	public void loadIgnoredCommand() throws IOException {
		loader.ignoreUnsupportedCommands();
		loader.load(new StringReader(COMMAND), model);
	}

	@Test
	public void loadZeroArguments() throws IOException {
		when(parser.min()).thenReturn(0);
		when(parser.max()).thenReturn(0);
		loader.add(COMMAND, parser);
		loader.load(new StringReader(COMMAND), model);
		verify(parser).parse(new String[]{COMMAND}, model);
	}

	@Test
	public void loadArguments() throws IOException {
		when(parser.min()).thenReturn(1);
		when(parser.max()).thenReturn(1);
		loader.add(COMMAND, parser);
		loader.load(new StringReader(COMMAND + " args"), model);
		verify(parser).parse(new String[]{COMMAND, "args"}, model);
	}

	@Test
	public void loadInsufficientArguments() throws IOException {
		when(parser.min()).thenReturn(1);
		when(parser.max()).thenReturn(1);
		loader.add(COMMAND, parser);
		assertThrows(IOException.class, () -> loader.load(new StringReader(COMMAND), model));
	}

	@Test
	public void loadSuperfluousArguments() throws IOException {
		loader.add(COMMAND, parser);
		assertThrows(IOException.class, () -> loader.load(new StringReader(COMMAND + " args"), model));
	}

	@Test
	public void arrayParser() {
		@SuppressWarnings("unchecked")
		final BiConsumer<Point, Object> setter = mock(BiConsumer.class);
		final var parser = new ArrayParser<>(3, Point::new, setter);
		parser.parse(new String[]{"command", "1", "2", "3"}, model);
		verify(setter).accept(new Point(1, 2, 3), model);
	}

	@Test
	public void arrayParserDefaultArguments() {
		// TODO
	}
}
