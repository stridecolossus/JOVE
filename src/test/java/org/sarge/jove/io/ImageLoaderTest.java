package org.sarge.jove.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

public class ImageLoaderTest {
	private ImageLoader loader;
	private ImageData image;

	@BeforeEach
	void before() {
		final Layout layout = new Layout("RGBA", Byte.class, 1, true);
		image = new ImageData(new Dimensions(2, 3), 1, 1, layout, Bufferable.of(new byte[2 * 3 * 4]));
		loader = new ImageLoader();
	}

	@Test
	void persist() throws IOException {
		// Write image array
		final ByteArrayOutputStream array = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(array);
		loader.save(image, out);

		// Load back again
		final DataInputStream in = new DataInputStream(new ByteArrayInputStream(array.toByteArray()));
		final ImageData result = loader.load(in);
		assertNotNull(result);

		// Check persisted image
		assertEquals(image.size(), result.size());
		assertEquals(image.data().length(), result.data().length());

		// Check layout
		assertEquals("RGBA", result.layout().components());
		assertEquals(1, result.layout().bytes());
		assertEquals(Byte.class, result.layout().type());
		assertEquals(true, result.layout().signed());
	}
}
