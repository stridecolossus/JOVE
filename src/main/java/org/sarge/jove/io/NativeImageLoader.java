package org.sarge.jove.io;

import static java.awt.image.BufferedImage.*;

import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import org.sarge.jove.common.*;

/**
 * Loader for a Java image implemented using {@link ImageIO}.
 * @author Sarge
 */
public class NativeImageLoader implements ResourceLoader<BufferedImage, ImageData> {
	@Override
	public BufferedImage map(InputStream in) throws IOException {
		return ImageIO.read(in);
	}

	@Override
	public ImageData load(BufferedImage image) throws IOException {
		// Validate image
		if(image == null) throw new IOException("Invalid image");

		// Determine component mapping
		final String components = switch(image.getType()) {
			case TYPE_BYTE_GRAY -> "R";
			case TYPE_4BYTE_ABGR, TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> "ABGR";
			default -> throw new RuntimeException("Unsupported image format: " + image);
		};

		// Extract image data array
		final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] bytes = buffer.getData();

		// Init image properties
		final Dimensions size = new Dimensions(image.getWidth(), image.getHeight());
		final Layout layout = new Layout(components.length(), Layout.Type.NORMALIZED, false, 1);
		// TODO - should be INTEGER? but VK format needs to be UNORM

		// Inject alpha channel as required
		final byte[] data = switch(image.getType()) {
			case TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> alpha(bytes, size.area());
			default -> bytes;
		};
		assert data.length == size.area() * layout.length();

		// Create image
		return new ImageData(size, components, layout, data);
	}

	/**
	 * Creates an image buffer with an injected alpha channel.
	 * @param bytes		RGB byte array
	 * @param size		Image size
	 * @return ABGR image data
	 */
	private static byte[] alpha(byte[] bytes, int size) {
		final byte[] result = new byte[bytes.length + size];
		int index = 0;
		for(int n = 0; n < bytes.length; n += 3) {
			result[index++] = Byte.MAX_VALUE;
			result[index++] = bytes[n];
			result[index++] = bytes[n + 1];
			result[index++] = bytes[n + 2];
		}
		assert index == result.length;
		return result;
	}
}
