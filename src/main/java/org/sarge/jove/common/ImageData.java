package org.sarge.jove.common;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.util.Check.notNull;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.Check;

/**
 * Wrapper for general image data.
 * @author Sarge
 */
public interface ImageData extends Bufferable {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Component sizes
	 */
	List<Integer> components();

	/**
	 * Default implementation.
	 */
	class DefaultImageData implements ImageData {
		private final Dimensions size;
		private final List<Integer> components;
		private final ByteBuffer data;

		/**
		 * Constructor.
		 * @param size				Size of this image
		 * @param components		Component sizes
		 * @param bytes				Image data
		 */
		public DefaultImageData(Dimensions size, int[] components, ByteBuffer data) {
			Check.notEmpty(components);
			final int expected = size.width() * size.height() * components.length; // TODO - assumes 8 bits per component
			if(expected != data.capacity()) throw new IllegalArgumentException("Buffer length does not match image dimensions");

			this.size = notNull(size);
			this.components = Arrays.stream(components).boxed().collect(toList());
			this.data = notNull(data);
		}

		@Override
		public Dimensions size() {
			return size;
		}

		@Override
		public List<Integer> components() {
			return components;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.put(data);
			data.flip();
		}

		@Override
		public long length() {
			return data.capacity();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", size)
					.append("components", components)
					.build();
		}
	}

	/**
	 * Loader for an image.
	 */
	class Loader implements org.sarge.jove.util.Loader<InputStream, ImageData> {
		private boolean add = true;

		/**
		 * Sets whether to enforce an alpha channel for RGB images.
		 * @param add Whether to add an alpha channel (default is {@code true})
		 */
		public void setEnforceAlpha(boolean add) {
			this.add = add;
		}

		/**
		 * Loads an image.
		 * @param name Image name
		 * @return Image
		 * @throws RuntimeException if the image cannot be loaded or the format is not supported
		 */
		@Override
		public ImageData load(InputStream in) {
			// Load image
			final BufferedImage image;
			try {
				image = ImageIO.read(in);
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
			if(image == null) {
				throw new RuntimeException("Invalid image");
			}

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

			// Create image wrapper
			final Dimensions dim = new Dimensions(result.getWidth(), result.getHeight());
			final int[] components = result.getColorModel().getComponentSize();
			final DataBufferByte data = (DataBufferByte) result.getRaster().getDataBuffer();
			return new DefaultImageData(dim, components, ByteBuffer.wrap(data.getData()));
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
