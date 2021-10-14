package org.sarge.jove.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.sarge.jove.util.ResourceLoader;

/**
 * The <i>image data</i> interface abstracts a native image.
 * <p>
 * The image {@link #layout()} specifies the number of channels comprising the image and the structure of each pixel.
 * For example a standard ABGR image with one byte per channel would have the following layout: <code>new Layout(4, Byte.class, 1, false)</code>
 * <p>
 * The {@link #mapping()} is a string representing the order of the channels, e.g. {@code ABGR} for a standard native image with an alpha channel.
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
	 * @return Component mapping specification
	 */
	String mapping();

	/**
	 * @return Image data
	 */
	byte[] bytes();

	/**
	 * Loader for an image.
	 */
	public static class Loader extends ResourceLoader.Adapter<BufferedImage, ImageData> {
		@Override
		protected BufferedImage map(InputStream in) throws IOException {
			final BufferedImage image = ImageIO.read(in);
			if(image == null) throw new IOException("Invalid image");
			return image;
		}

		@Override
		public ImageData load(BufferedImage image) {
			return wrap(convert(image));
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
					final int num = image.getColorModel().getNumComponents();
					return new Layout(num, Byte.class, 1, false);
				}

				@Override
				public String mapping() {
					return switch(image.getType()) {
						case BufferedImage.TYPE_BYTE_GRAY -> "RRR1";		// TODO
						case BufferedImage.TYPE_4BYTE_ABGR -> "ABGR";
						default -> throw new RuntimeException();
					};
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
