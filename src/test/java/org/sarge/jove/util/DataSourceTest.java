package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
	private DataSource src;
	private Path root;
	private InputStream in;
	private ResourceLoader<InputStream, String> loader;

	@BeforeEach
	void before() throws IOException {
		// Init root
		root = mock(Path.class);
		when(root.resolve(anyString())).thenReturn(root);

		// Init file system
		final FileSystem sys = mock(FileSystem.class);
		when(root.getFileSystem()).thenReturn(sys);

		// Init provider
		final FileSystemProvider provider = mock(FileSystemProvider.class);
		when(sys.provider()).thenReturn(provider);

		// Init input stream
		in = mock(InputStream.class);
		when(provider.newInputStream(root)).thenReturn(in);

		// Create data source
		src = new DataSource(root);

		// Init resource loader
		loader = mock(ResourceLoader.class);
	}

	@Test
	void constructorInvalidDirectory() {
		assertThrows(IllegalArgumentException.class, () -> DataSource.of("cobblers"));
	}

	@Test
	void load() throws IOException {
		// Init resource path
		final String name = "name";
		when(root.resolve(name)).thenReturn(root);

		// Create loader
		when(loader.map(in)).thenReturn(in);
		when(loader.load(in)).thenReturn(name);

		// Load resource
		assertEquals(name, src.load(name, loader));
	}

	@Test
	void loadInvalidResource() {
		when(root.resolve(anyString())).thenThrow(InvalidPathException.class);
		assertThrows(RuntimeException.class, () -> src.load("cobblers", loader));
	}

	@Test
	void loadMapFail() throws IOException {
		when(loader.map(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load("cobblers", loader));
	}

	@Test
	void loadLoaderFail() throws IOException {
		when(loader.map(in)).thenReturn(in);
		when(loader.load(in)).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load("cobblers", loader));
	}
}
