package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.io.ZipDataSource;

public class ZipDataSourceTest {
	private ZipDataSource src;
	private ZipFile file;

	@BeforeEach
	void before() {
		file = mock(ZipFile.class);
		src = new ZipDataSource(file);
	}

	@Test
	void input() throws IOException {
		// Create entry
		final String name = "name";
		final ZipEntry entry = mock(ZipEntry.class);
		when(file.getEntry(name)).thenReturn(entry);

		// Check input stream
		final InputStream in = mock(InputStream.class);
		when(file.getInputStream(entry)).thenReturn(in);
		assertEquals(in, src.input(name));
	}

	@Test
	void output() {
		assertThrows(UnsupportedOperationException.class, () -> src.output("whatever"));
	}
}
