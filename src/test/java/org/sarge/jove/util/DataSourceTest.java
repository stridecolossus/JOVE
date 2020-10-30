package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
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
		try(final InputStream in = src.open("duke.jpg")) {
			assertNotNull(in);
		}
	}

	@Test
	void openInvalidFilename() throws IOException {
		assertThrows(NoSuchFileException.class, () -> src.open("cobblers"));
	}

	@SuppressWarnings({"resource", "unchecked"})
	@Test
	void loader() throws IOException {
		final Loader<InputStream, Object> delegate = mock(Loader.class);
		final var loader = DataSource.loader(src, delegate);
		assertNotNull(loader);
		loader.load("duke.jpg");
		verify(delegate).load(isA(InputStream.class));
	}
}
