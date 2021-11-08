package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.io.DataSource;
import org.sarge.jove.io.ResourceLoaderWriter;

public class DataSourceTest {
	private static final String NAME = "name";

	private DataSource src;
	private ResourceLoaderWriter<InputStream, OutputStream, String> loader;

	@BeforeEach
	void before() throws IOException {
		loader = mock(ResourceLoaderWriter.class);
		src = spy(DataSource.class);
	}

	@Test
	void load() throws Exception {
		final InputStream in = mock(InputStream.class);
		when(src.input(NAME)).thenReturn(in);
		src.load(NAME, loader);
		verify(loader).map(in);
		verify(loader).load(null);
	}

	@Test
	void loadMapFailed() throws Exception {
		final InputStream in = mock(InputStream.class);
		when(src.input(NAME)).thenReturn(in);
		when(loader.map(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load(NAME, loader));
	}

	@Test
	void loadLoaderFailed() throws Exception {
		final InputStream in = mock(InputStream.class);
		when(src.input(NAME)).thenReturn(in);
		when(loader.load(null)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load(NAME, loader));
	}

	@Test
	void save() throws Exception {
		final OutputStream out = mock(OutputStream.class);
		when(src.output(NAME)).thenReturn(out);
		src.write(NAME, NAME, loader);
		verify(loader).map(out);
		verify(loader).write(NAME, null);
	}
}
