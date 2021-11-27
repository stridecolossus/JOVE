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
import org.sarge.jove.platform.vulkan.VkFormat;

public class VulkanImageLoaderTest {
	private static final int FORMAT = VkFormat.R8G8B8A8_SRGB.value();

	private VulkanImageLoader loader;
	private DataInputStream in;
	private DataOutputStream out;
	private ByteArrayOutputStream file;

	@BeforeEach
	void before() throws IOException {
		file = new ByteArrayOutputStream();
		in = null;
		out = new DataOutputStream(file);
		loader = new VulkanImageLoader();
	}

	/**
	 * Copies file to input stream.
	 */
	private void init() throws IOException {
		in = new DataInputStream(new ByteArrayInputStream(file.toByteArray()));
	}

	/**
	 * Writes the KTX header.
	 */
	private void header(int typeSize, int layerCount, int faceCount, int scheme) throws IOException {
		// Write header
		final byte[] str = "KTX 20".getBytes();
		final byte[] header = new byte[12];
		System.arraycopy(str, 0, header, 3, str.length);
		out.write(header);

		// Write image header
		out.writeInt(FORMAT);
		out.writeInt(typeSize);

		// Write image extents
		out.writeInt(2);
		out.writeInt(3);
		out.writeInt(0);

		// Write image sizes (2 MIP levels)
		out.writeInt(layerCount);
		out.writeInt(faceCount);
		out.writeInt(2);
		out.writeInt(scheme);
	}

	@Test
	void load() throws IOException {
		// Write header
		header(1, 1, 1, 0);

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
		init();
		final ImageData image = loader.load(in);
		assertNotNull(image);

		// Check image
		assertEquals(new Extents(new Dimensions(2, 3)), image.extents());
		assertEquals("RGBA", image.components());
		assertEquals(FORMAT, image.format());
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
		init();
		assertThrows(UnsupportedOperationException.class, () -> loader.load(in));
	}

	@Test
	void loadInvalidTypeSize() throws IOException {
		header(999, 1, 1, 0);
		init();
		assertThrows(UnsupportedOperationException.class, () -> loader.load(in));
	}

	@Test
	void loadInvalidLayerCount() throws IOException {
		header(1, 999, 1, 0);
		init();
		assertThrows(UnsupportedOperationException.class, () -> loader.load(in));
	}

	@Test
	void loadInvalidCubemap() throws IOException {
		header(1, 6, 1, 0);
		init();
		assertThrows(UnsupportedOperationException.class, () -> loader.load(in));
	}

	@Test
	void loadUnsupportedCompressionScheme() throws IOException {
		header(1, 1, 1, 999);
		init();
		assertThrows(UnsupportedOperationException.class, () -> loader.load(in));
	}

	@Test
	void loadDescriptor() throws IOException {
		// Write DFD size
		final int size = 24 + 4 * 16;
		out.writeInt(size);

		// Write header
		out.writeShort(0);					// Vendor
		out.writeShort(0);					// Type
		out.writeShort(2);					// Version
		out.writeShort(size);				// Block size
		out.writeByte(1);					// Model (RGBSDA)
		out.writeByte(1);					// Primaries (BT709)
		out.writeByte(2);					// Transfer function (SRGB)
		out.writeByte(0);					// Flags

		// Write texel dimensions (unused)
		final byte[] array = new byte[4];
		out.write(array);

		// Write planes (unused)
		out.write(array);
		out.write(array);

		// Write samples
		final byte[] channels = {0, 1, 2, 31};
		for(int n = 0; n < 4; ++n) {
			out.writeShort(0);				// Offset (unused)
			out.writeByte(7);				// Bit length
			out.writeByte(channels[n]);		// Channel
			out.writeInt(0);				// Sample position (unused)
			out.writeInt(0);				// Upper (unused)
			out.writeInt(0);				// Lower (unused)
		}

		// Load descriptor
		init();
		assertEquals("RGBA", loader.loadFormatDescriptor(in, size));
	}

	@Test
	void loadDescriptorInvalidLength() throws IOException {
		out.writeInt(999);
		init();
		assertThrows(IllegalArgumentException.class, () -> loader.loadFormatDescriptor(in, 1));
	}

	@Test
	void loadKeyValues() throws IOException {
		// Write entry length
		out.writeInt(18);

		// Write entry
		out.write("KTXorientation".getBytes());
		out.write(' ');
		out.write("rd".getBytes());

		// Write padding bytes
		final int padding = 2 + 4;
		for(int n = 0; n < padding; ++n) {
			out.write(0);
		}

		// Load key-values
		init();
		final List<byte[]> entries = loader.loadKeyValues(in, 18 + padding);

		// Check entry
		assertNotNull(entries);
		assertEquals(1, entries.size());
		assertEquals("KTXorientation rd", new String(entries.get(0)).trim());
	}
}
