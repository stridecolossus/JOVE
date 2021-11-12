package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceLoaderAdapterTest {
	private static final String NAME = "name";

	private ResourceLoaderAdapter<InputStream, Object> adapter;
	private ResourceLoader<InputStream, Object> loader;
	private DataSource src;
	private InputStream in;

	@BeforeEach
	void before() throws IOException {
		// Create data source
		src = mock(DataSource.class);
		in = mock(InputStream.class);
		when(src.input(NAME)).thenReturn(in);

		// Create loader
		loader = mock(ResourceLoader.class);
		when(loader.map(in)).thenReturn(in);

		// Create loader adapter
		adapter = new ResourceLoaderAdapter<>(src, loader);
	}

	@Test
	void load() throws IOException {
		final Object obj = new Object();
		when(loader.load(in)).thenReturn(obj);
		assertEquals(obj, adapter.load(NAME));
	}

	@Test
	void loadDataSourceError() throws IOException {
		when(src.input(NAME)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> adapter.load(NAME));
	}

	@Test
	void loadMapperError() throws IOException {
		when(loader.map(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> adapter.load(NAME));
	}

	@Test
	void loadLoaderError() throws IOException {
		when(loader.load(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> adapter.load(NAME));
	}
}
