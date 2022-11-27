package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;

import org.junit.jupiter.api.*;

@SuppressWarnings({"resource", "unchecked"})
public class ResourceLoaderAdapterTest {
	private static final String NAME = "name";

	private ResourceLoaderAdapter<InputStream, Object> adapter;
	private ResourceLoader<InputStream, Object> loader;
	private DataSource src;
	private InputStream in;

	@BeforeEach
	void before() throws Exception {
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
	void load() throws Exception {
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
	void loadMapperError() throws Exception {
		when(loader.map(in)).thenThrow(Exception.class);
		assertThrows(RuntimeException.class, () -> adapter.load(NAME));
	}

	@Test
	void loadLoaderError() throws Exception {
		when(loader.load(in)).thenThrow(Exception.class);
		assertThrows(RuntimeException.class, () -> adapter.load(NAME));
	}
}
