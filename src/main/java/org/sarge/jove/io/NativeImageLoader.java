package org.sarge.jove.io;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.awt.image.BufferedImage.TYPE_BYTE_INDEXED;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

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
		final String mapping = switch(image.getType()) {
			case TYPE_BYTE_GRAY -> "R"; 		// TODO - will this work?
			case TYPE_4BYTE_ABGR, TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> "ABGR";
			default -> throw new RuntimeException("Unsupported image format: " + image);
		};

		// Extract image data array
		final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] bytes = buffer.getData();

		// Init image properties
		final Dimensions size = new Dimensions(image.getWidth(), image.getHeight());
		final Layout layout = new Layout(mapping.length(), Byte.class, 1, false);

		// Wrap image data
		final Bufferable data = switch(image.getType()) {
			case TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> alpha(bytes, size.area());
			default -> Bufferable.of(bytes);
		};

		// Create image
		// TODO - mip levels
		return new ImageData(size, 1, 1, layout, mapping, data);
	}

	/**
	 * Creates an image buffer with an injected alpha channel.
	 * @param bytes		RGB byte array
	 * @param size		Image size
	 * @return RGBA image data
	 */
	private static Bufferable alpha(byte[] bytes, int size) {
		return new Bufferable() {
			@Override
			public int length() {
				return bytes.length + size;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				for(int n = 0; n < bytes.length; n += 3) {
					bb.put(Byte.MAX_VALUE);
					bb.put(bytes[n]);
					bb.put(bytes[n + 1]);
					bb.put(bytes[n + 2]);
				}
			}
		};
	}
}