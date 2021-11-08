package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.io.FileDataSource;

public class FileDataSourceTest {
	private static final String NAME = "name";

	private FileDataSource src;
	private Path root;

	@BeforeEach
	void before() throws IOException {
		root = Files.createTempDirectory("DataSourceTest");
		src = new FileDataSource(root);
		Files.createFile(root.resolve(NAME));
	}

	@Test
	void constructor() {
		assertEquals(root, src.root());
	}

	@Test
	void constructorInvalidRoot() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> new FileDataSource("cobblers"));
	}

	@Test
	void home() {
		final FileDataSource home = FileDataSource.home(null);
		assertNotNull(home);
	}

	@Test
	void input() throws IOException {
		assertNotNull(src.input(NAME));
	}

	@Test
	void inputResourceNotFound() throws IOException {
		assertThrows(NoSuchFileException.class, () -> src.input("cobblers"));
	}

	@Test
	void output() throws IOException {
		assertNotNull(src.output(NAME));
	}
}
