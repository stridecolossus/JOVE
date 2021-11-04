package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataSourceTest {
	private static final String NAME = "name";

	private DataSource src;
	private Path root;
	private ResourceLoaderWriter<InputStream, OutputStream, String> loader;

	@BeforeEach
	void before() throws IOException {
		loader = mock(ResourceLoaderWriter.class);
		root = Files.createTempDirectory("DataSourceTest");
		src = new DataSource(root);
	}

	@Test
	void constructor() {
		assertEquals(root, src.root());
	}

	@Test
	void constructorInvalidRoot() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> new DataSource("cobblers"));
	}

	@Test
	void home() {
		final DataSource home = DataSource.home(null);
		assertNotNull(home);
	}

	@Test
	void resolve() throws IOException {
		final Path path = Files.createTempFile(root, NAME, null);
		final DataSource sub = src.resolve(path.getFileName());
		assertNotNull(sub);
		assertEquals(path, sub.root());
	}

	@Test
	void load() throws IOException {
		final Path path = Files.createTempFile(root, NAME, null);
		final String name = path.getFileName().toString();
		src.load(name, loader);
		verify(loader).map(isA(InputStream.class));
		verify(loader).load(null);
	}

	@Test
	void loadResourceNotFound() throws IOException {
		assertThrows(RuntimeException.class, "Error loading resource", () -> src.load("cobblers", loader));
	}

	@Test
	void loadMapFailed() throws IOException {
		when(loader.map(any(InputStream.class))).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load(NAME, loader));
	}

	@Test
	void loadLoaderFailed() throws IOException {
		when(loader.load(any(InputStream.class))).thenThrow(IOException.class);
		assertThrows(RuntimeException.class, () -> src.load(NAME, loader));
	}

	@Test
	void write() throws IOException {
		src.write(NAME, NAME, loader);
		verify(loader).map(isA(OutputStream.class));
		verify(loader).write(NAME, null);
	}
}
