package org.sarge.jove.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

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
	byte[] bytes();

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
			if(image == null) throw new IOException("Invalid image");
			final BufferedImage result = convert(image);
			return wrap(result);
		}

		/**
		 * Adds an alpha channel as appropriate.
		 */
		private static BufferedImage convert(BufferedImage image) {
			return switch(image.getType()) {
				case BufferedImage.TYPE_BYTE_GRAY -> image;
				case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_INDEXED -> alpha(image);
				case BufferedImage.TYPE_4BYTE_ABGR -> image;
				default -> throw new RuntimeException("Unsupported image format: " + image);
			};
		}

		/**
		 * @return New wrapper for the given buffered image
		 */
		private static ImageData wrap(BufferedImage image) {
			return new ImageData() {
				@Override
				public Dimensions size() {
					return new Dimensions(image.getWidth(), image.getHeight());
				}

				@Override
				public Layout layout() {
					final String mapping = switch(image.getType()) {
						case BufferedImage.TYPE_BYTE_GRAY -> "RRR1";
						default -> {
							final int num = image.getColorModel().getNumComponents();
							yield "ABGR".substring(0, num);
						}
					};
					return new Layout(mapping, Byte.class, 1, false);
				}

				@Override
				public byte[] bytes() {
					final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
					return buffer.getData();
				}
			};
		}

		/**
		 * Adds an alpha channel to an image.
		 * @param image Image
		 * @return Image with alpha channel
		 */
		private static BufferedImage alpha(BufferedImage image) {
			final BufferedImage alpha = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics g = alpha.getGraphics();
			try {
				g.drawImage(image, 0, 0, null);
			}
			finally {
				g.dispose();
			}
			return alpha;
		}
	}
}
