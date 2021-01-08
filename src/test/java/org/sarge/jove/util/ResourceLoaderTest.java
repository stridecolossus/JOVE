package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.util.ResourceLoader.Adapter;

public class ResourceLoaderTest {
	private static final String FILENAME = "filename";

	private ResourceLoader<String, Object> loader;
	private Adapter<InputStream, Object> adapter;
	private DataSource src;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		src = mock(DataSource.class);
		adapter = mock(Adapter.class);
		loader = ResourceLoader.of(src, adapter);
	}

	@Test
	void constructor() {
		assertNotNull(loader);
	}

	@SuppressWarnings("resource")
	private InputStream init() throws IOException {
		final InputStream in = mock(InputStream.class);
		when(src.open(FILENAME)).thenReturn(in);
		when(adapter.map(in)).thenReturn(in);
		return in;
	}

	@SuppressWarnings("resource")
	@Test
	void load() throws IOException {
		final InputStream in = init();
		loader.load(FILENAME);
		verify(adapter).load(in);
	}

	@SuppressWarnings("resource")
	@Test
	void loadCannotOpen() throws IOException {
		when(src.open(FILENAME)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> loader.load(FILENAME));
	}

	@SuppressWarnings("resource")
	@Test
	void loadCannotLoad() throws IOException {
		final InputStream in = init();
		when(adapter.load(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> loader.load(FILENAME));
	}
}
