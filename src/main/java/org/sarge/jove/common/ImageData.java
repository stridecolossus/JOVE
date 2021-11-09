package org.sarge.jove.common;

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

import org.sarge.jove.io.ResourceLoader;

/**
 * The <i>image data</i> interface abstracts a native image.
 * <p>
 * The image {@link #layout()} specifies the number of channels comprising the image and the structure of each pixel.
 * For example a standard ABGR image with one byte per channel would have the following layout: <code>new Layout("ABGR", Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Image layout
	 */
	Layout layout();

	/**
	 * @return Image data
	 */
	Bufferable buffer();

	/**
	 * Loader for an image.
	 */
	public static class Loader implements ResourceLoader<BufferedImage, ImageData> {
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
				case TYPE_BYTE_GRAY -> "RRR1";
				case TYPE_4BYTE_ABGR, TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> "ABGR";
				default -> throw new RuntimeException("Unsupported image format: " + image);
			};

			// Extract image data array
			final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
			final byte[] bytes = buffer.getData();

			// Create image wrapper
			return new ImageData() {
				@Override
				public Dimensions size() {
					return new Dimensions(image.getWidth(), image.getHeight());
				}

				@Override
				public Layout layout() {
					return new Layout(components, Byte.class, 1, false);
				}

				@Override
				public Bufferable buffer() {
					return switch(image.getType()) {
						case TYPE_3BYTE_BGR, TYPE_BYTE_INDEXED -> {
							final int size = this.size().area();
							yield alpha(bytes, size);
						}
						default -> Bufferable.of(bytes);
					};
				}
			};
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
}
