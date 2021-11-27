package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.io.ImageData.Level;

public class VulkanImageLoaderTest {
	private VulkanImageLoader loader;
	private DataInputStream in;
	private DataOutputStream out;
	private ByteArrayOutputStream file;

	@BeforeEach
	void before() throws IOException {
		file = new ByteArrayOutputStream();
		out = new DataOutputStream(file);
		loader = new VulkanImageLoader();
	}

	private ImageData run() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(file.toByteArray())));
	}

	private byte[] header() {
		final byte[] str = "KTX 20".getBytes();
		final byte[] array = new byte[12];
		System.arraycopy(str, 0, array, 3, str.length);
		return array;
	}

	// TODO
	// - faces
	// - invalid cubemap
	// - invalid layers, type, schema

	@Test
	void load() throws IOException {
		// Write KTX header
		out.write(header());

		// Write image header
		out.writeInt(43);		// Format: R8G8B8A8_SRGB
		out.writeInt(1);		// Type size
		out.writeInt(2);		// Width
		out.writeInt(3);		// Height
		out.writeInt(0);		// Depth -> 1
		out.writeInt(0);		// Layers -> 1
		out.writeInt(1);		// Faces
		out.writeInt(2);		// MIP levels
		out.writeInt(0);		// Compression scheme (none)

		// Write format descriptor offsets
		out.writeInt(0);
		out.writeInt(0);

		// Write key-value offsets
		out.writeInt(0);
		out.writeInt(0);

		// Write compression offsets
		out.writeLong(0);
		out.writeLong(0);

		// Write levels index (two MIP levels, largest first)
		final int len = 2 * 3 * 4;
		final int half = 1 * 2 * 4;
		out.writeLong(5 + half);
		out.writeLong(len);
		out.writeLong(len);
		out.writeLong(5);
		out.writeLong(half);
		out.writeLong(half);

		// Write format descriptor size
		out.writeInt(0);

		// Write image data
		final int total = len + half;
		out.write(new byte[total]);

		// Load image
		final ImageData image = run();
		assertNotNull(image);

		// Check image
		assertEquals(new Extents(new Dimensions(2, 3)), image.extents());
		assertEquals("RGBA", image.components());
		assertEquals(Layout.bytes(4), image.layout());
		assertEquals(1, image.layers());

		// Check MIP levels (ordered by MIP level with offsets smallest first)
		final List<Level> expected = List.of(new Level(half, len), new Level(0, half));
		assertEquals(expected, image.levels());

		// Check image data
		assertNotNull(image.data(0));
		assertEquals(total, image.data(0).length());
	}

	@Test
	void loadInvalidHeader() throws IOException {
		out.write(new byte[12]);
		assertThrows(UnsupportedOperationException.class, () -> run());
	}
}
