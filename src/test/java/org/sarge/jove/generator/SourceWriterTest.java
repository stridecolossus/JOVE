package org.sarge.jove.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SourceWriterTest {
	private SourceWriter writer;
	private Path dir;
	private Path file;

	@BeforeEach
	public void before() {
		dir = Paths.get(System.getProperty("java.io.tmpdir"));
		file = dir.resolve("name.java");
		writer = new SourceWriter(dir);
	}

	@AfterEach
	public void after() {
		file.toFile().delete();
	}

	@Test
	public void constructor() {
		assertEquals(0, writer.count());
	}

	@Test
	public void write() throws IOException {
		writer.write("name", "source");
		assertEquals(true, file.toFile().exists());
		assertEquals(1, writer.count());
	}

	@Test
	public void replaceIgnore() throws IOException {
		writer.write("name", "before");
		writer.replace(false);
		writer.write("name", "after");
		assertEquals("before", Files.readString(file));
		assertEquals(1, writer.count());
	}
}
