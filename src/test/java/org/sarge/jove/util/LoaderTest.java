package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.sarge.jove.util.Loader.DataSource;

public class LoaderTest {
	@Test
	void directory() throws IOException {
		// Create data-source
		final DataSource src = DataSource.of("./src/test/resources");
		assertNotNull(src);

		// Open resource
		try(final InputStream in = src.apply("thiswayup.jpg")) {
			assertNotNull(in);
		}
	}

	@Test
	void unknownDirectory() {
		assertThrows(IllegalArgumentException.class, () -> DataSource.of("cobblers"));
	}

	@SuppressWarnings({"unchecked", "resource"})
	@Test
	void compose() {
		final DataSource src = mock(DataSource.class);
		final Loader<InputStream, Object> loader = mock(Loader.class);
		final var adapter = Loader.of(src, loader);
		final String name = "name";
		adapter.load(name);
		verify(src).apply(name);
		verify(loader).load(null);
	}
}
