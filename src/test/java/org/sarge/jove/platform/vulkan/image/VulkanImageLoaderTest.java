package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.ImageData;

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
		out.writeInt(1);		// MIP levels
		out.writeInt(0);		// Compression scheme (none)

		// Write format descriptor offsets
		out.writeInt(999);
		out.writeInt(0);

		// Write key-value offsets
		out.writeInt(999);
		out.writeInt(0);

		// Write compression offsets
		out.writeLong(0);
		out.writeLong(0);

		// Write levels index
		final int len = 2 * 3 * 4;
		out.writeLong(0);
		out.writeLong(len);
		out.writeLong(len);

		// Write format descriptor size
		out.writeInt(0);

		// Write image data
		out.write(new byte[len]);

		// Load image
		final ImageData image = run();
		assertNotNull(image);

		assertEquals(new Dimensions(2, 3), image.size());
		assertEquals("RGBA", image.components());
		assertEquals(Layout.bytes(4), image.layout());
		assertEquals(1, image.levels());
		assertEquals(1, image.levels());

		assertNotNull(image.data(0, 0));
		assertEquals(len, image.data(0, 0).length());
	}

	@Test
	void loadInvalidHeader() throws IOException {
		out.write(new byte[12]);
		assertThrows(IOException.class, () -> run());
	}
}
