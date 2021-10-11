package org.sarge.jove.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.sarge.jove.common.Vertex.Layout;
import org.sarge.jove.util.ResourceLoader;

/**
 * The <i>image data</i> interface abstracts an RGBA texture.
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
	public static class Loader extends ResourceLoader.Adapter<BufferedImage, ImageData> {
		private boolean add = true;

		/**
		 * Sets whether to enforce an alpha channel for RGB images.
		 * @param add Whether to add an alpha channel (default is {@code true})
		 */
		public void setEnforceAlpha(boolean add) {
			this.add = add;
		}

		@Override
		protected BufferedImage map(InputStream in) throws IOException {
			final BufferedImage image = ImageIO.read(in);
			if(image == null) throw new IOException("Invalid image");
			return image;
		}

		/**
		 * Loads an image.
		 * @throws RuntimeException if the image cannot be loaded or the format is not supported
		 */
		@Override
		public ImageData load(BufferedImage image) {
			// Convert image
			final BufferedImage result = switch(image.getType()) {
				// Gray-scale
				case BufferedImage.TYPE_BYTE_GRAY -> {
					yield image;
				}

				// RGB
				case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_INDEXED -> {
					if(add) {
						yield swizzle(alpha(image));
					}
					else {
						yield swizzle(image);
					}
				}

				// RGBA
				case BufferedImage.TYPE_4BYTE_ABGR -> swizzle(alpha(image));

				// Unknown
				default -> throw new RuntimeException("Unsupported image format: " + image);
			};

			// TODO - assumes:
			// - bytes
			// - all same size

//			// Determine data type
//			final Class<?> type = switch(buffer.getDataType()) {
//				case DataBuffer.TYPE_BYTE -> Byte.class;
//				case DataBuffer.TYPE_USHORT -> Short.class;
//				default -> throw new RuntimeException("Unsupported image data type: " + 42);
//			};

			// Create image wrapper
			return new ImageData() {
				@Override
				public Dimensions size() {
					return new Dimensions(result.getWidth(), result.getHeight());
				}

				@Override
				public Layout layout() {
					final int num = result.getColorModel().getNumColorComponents();
					return Layout.of(num == 1 ? 1 : 4, Byte.class);
				}

				@Override
				public byte[] bytes() {
					final DataBufferByte buffer = (DataBufferByte) result.getRaster().getDataBuffer();
					return buffer.getData();
				}
			};
		}

		/**
		 * Converts the given image to an RGBA format.
		 * @param image Image
		 * @return RGBA image
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

		/**
		 * Converts ABGR to RGBA.
		 * @param image Image
		 * @return RGBA image
		 */
		private static BufferedImage swizzle(BufferedImage image) {
			final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
			final byte[] bytes = data.getData();
			for(int n = 0; n < bytes.length; n += 4) {
				swap(bytes, n, 0, 3);
				swap(bytes, n, 1, 2);
			}
			return image;
		}

		/**
		 * Swaps a BGRA pixel to RGBA.
		 * @param bytes			Image data
		 * @param index			Pixel index
		 * @param src			Source component
		 * @param dest			Destination component
		 */
		private static void swap(byte[] bytes, int index, int src, int dest) {
			final int a = index + src;
			final int b = index + dest;
			final byte temp = bytes[a];
			bytes[a] = bytes[b];
			bytes[b] = temp;
		}
	}
}
