package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class ResourceLoaderTest {
	@Test
	void of() throws IOException {
		// Create loader adapter
		final DataSource src = mock(DataSource.class);
		final ResourceLoader<Object, Object> loader = mock(ResourceLoader.class);
		final var adapter = ResourceLoader.of(src, loader);
		assertNotNull(adapter);

		// Init data source
		final String name = "filename";
		final InputStream in = mock(InputStream.class);
		when(src.open(name)).thenReturn(in);

		// Init mapper
		final Object data = new Object();
		when(loader.map(in)).thenReturn(data);

		// Init loader
		final Object obj = new Object();
		when(loader.load(data)).thenReturn(obj);

		// Invoke loader
		assertEquals(obj, adapter.apply(name));
	}
}
