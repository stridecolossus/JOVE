package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

	@SuppressWarnings({"resource", "unchecked"})
	@Nested
	class LoaderTests {
		@Test
		void loader() throws IOException {
			// Create delegate loader
			final Object input = new Object();
			final Loader.Adapter<Object, ?> delegate = mock(Loader.Adapter.class);
			when(delegate.map(isA(InputStream.class))).thenReturn(input);

			// Create loader for this data-source
			final var loader = src.loader(delegate);
			assertNotNull(loader);

			// Check loader
			loader.load(FILENAME);
			verify(delegate).load(input);
		}

		@Test
		void loaderMappingError() throws IOException {
			final Loader.Adapter<Object, ?> delegate = mock(Loader.Adapter.class);
			when(delegate.map(any())).thenThrow(IOException.class);
			assertThrows(RuntimeException.class, () -> src.loader(delegate).load(FILENAME));
		}

		@Test
		void loaderLoaderError() throws IOException {
			final Loader.Adapter<Object, ?> delegate = mock(Loader.Adapter.class);
			when(delegate.load(any())).thenThrow(IOException.class);
			assertThrows(RuntimeException.class, () -> src.loader(delegate).load(FILENAME));
		}
	}
}
