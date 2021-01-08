package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
	private static final String FILENAME = "duke.jpg";

	private DataSource src;

	@BeforeEach
	void before() {
		src = DataSource.of("./src/test/resources");
	}

	@Test
	void constructor() {
		assertNotNull(src);
	}

	@Test
	void constructorInvalidDirectory() {
		assertThrows(IllegalArgumentException.class, () -> DataSource.of("cobblers"));
	}

	@Test
	void open() throws IOException {
		try(final InputStream in = src.open(FILENAME)) {
			assertNotNull(in);
		}
	}

	@Test
	void openInvalidFilename() throws IOException {
		assertThrows(NoSuchFileException.class, () -> src.open("cobblers"));
	}
}
