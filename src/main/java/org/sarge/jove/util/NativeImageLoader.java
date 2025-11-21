package org.sarge.jove.util;

import static java.awt.image.BufferedImage.*;

import java.awt.image.*;
import java.io.IOException;
import java.nio.file.*;

import javax.imageio.ImageIO;

import org.sarge.jove.common.*;

/**
 * The <i>native image loader</i> uses the {@link ImageIO} library to load images from the file system.
 * @author Sarge
 */
public class NativeImageLoader {
	/**
	 * Loads an image from the given path.
	 * @param path Image path
	 * @return Image
	 * @throws IOException if the image format is unsupported
	 */
	public ImageData load(Path path) throws IOException {
		try(var in = Files.newInputStream(path)) {
			final BufferedImage image = ImageIO.read(in);
			if(image == null) {
				throw new IOException("Unsupported image format: " + path);
			}
			return load(image);
		}
	}

	/**
	 * Converts a Java image to a generic JOVE image.
	 * TODO - doc injects alpha channel
	 * TODO - alpha injection should be optional?
	 * @param image Java image
	 * @return JOVE image
	 * @throws RuntimeException if the image format is unsupported
	 */
	public ImageData load(BufferedImage image) {
		// Determine image channels
		final String channels = switch(image.getType()) {
			case TYPE_BYTE_GRAY -> "R";
			case TYPE_4BYTE_ABGR, TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> "ABGR";
			default -> throw new RuntimeException("Unsupported image format: " + image);
		};

		// Extract image data array
		final var buffer = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] bytes = buffer.getData();

		// Init image properties
		final Dimensions size = new Dimensions(image.getWidth(), image.getHeight());
		final Layout layout = new Layout(channels.length(), Layout.Type.NORMALIZED, false, 1);
		// TODO - should be INTEGER? but VK format needs to be UNORM

		// Inject alpha channel as required
		final byte[] data = switch(image.getType()) {
			case TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> alpha(bytes, size.area());
			default -> bytes;
		};
		assert data.length == size.area() * layout.stride();

		// Create image
		return new ImageData(size, channels, layout, data);
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
