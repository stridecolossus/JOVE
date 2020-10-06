package org.sarge.jove.common;

import static org.sarge.jove.util.Check.notNull;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.BufferFactory;
import org.sarge.jove.util.DataSource;

/**
 * Wrapper for RGBA image data.
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Image data
	 */
	ByteBuffer buffer();

	/**
	 * Default implementation.
	 * Note that the underlying array is mutable.
	 */
	class DefaultImageData implements ImageData {
		private final Dimensions size;
		private final ByteBuffer data;

		/**
		 * Constructor.
		 * @param size		Dimensions
		 * @param bytes		Image data
		 */
		DefaultImageData(Dimensions size, ByteBuffer data) {
			if(size.width() * size.height() * 4 != data.capacity()) throw new IllegalArgumentException("Buffer length does not match image dimensions");
			this.size = notNull(size);
			this.data = notNull(data);
		}

		@Override
		public Dimensions size() {
			return size;
		}

		@Override
		public ByteBuffer buffer() {
			return data.flip().asReadOnlyBuffer();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("size", size).build();
		}
	}

	/**
	 * Loader for images.
	 */
	public static class Loader {
		/**
		 * Image converters.
		 */
		private static final Map<Integer, Function<byte[], ByteBuffer>> CONVERTERS = Map.of(
				BufferedImage.TYPE_3BYTE_BGR,
				src -> {
					new Swizzle(0, 2).apply(src, 3);
					return alpha(src, 3, Byte.MAX_VALUE);
				},

				BufferedImage.TYPE_4BYTE_ABGR,
				src -> {
					new Swizzle(0, 3).apply(src, 4);		// A-R
					new Swizzle(1, 2).apply(src, 4);		// B-G
					return ByteBuffer.wrap(src);
				}
		);
		// TODO - make this configurable

		private final DataSource src;

		/**
		 * Constructor.
		 * @param src Data source
		 */
		public Loader(DataSource src) {
			this.src = notNull(src);
		}

//		/**
//		 * Determines the number of mipmap levels for the given image dimensions.
//		 * @param dim Image dimensions
//		 * @return Number of mipmap levels
//		 */
//		public static int levels(Dimensions dim) {
//			final float max = Math.max(dim.width(), dim.height());
//			return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
//		}

		/**
		 * Loads an image.
		 * @param name Image name
		 * @return Image
		 * @throws IOException if the image cannot be loaded or the format is not supported by this loader
		 */
		public ImageData load(String name) throws IOException {
			// Load raw image
			final BufferedImage image;
			try(final InputStream in = src.apply(name)) {
				image = ImageIO.read(in);
			}

			// Lookup image converter
			final var converter = CONVERTERS.get(image.getType());
			if(converter == null) throw new IOException("Unsupported image type: " + image);

			// Convert image to buffer
			// TODO - handle other types (int, etc)
			final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
			final ByteBuffer bb = converter.apply(buffer.getData());

			// Create image wrapper
			final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
			return new DefaultImageData(dim, bb);
		}

		/**
		 * An <i>image swizzle</i> is used to swap components of an image byte array.
		 * <p>
		 * For example, to transform a BGR image to RGB:
		 * <pre>
		 *  byte[] bytes = ...
		 *  Swizzle swizzle = new Swizzle(0, 2);		// Swap the R and G components
		 *  swizzle.apply(bytes, 3);					// Apply to 3-component sized image
		 * </pre>
		 */
		public record Swizzle(int src, int dest) {
			/**
			 * Applies this swizzle to the given byte-array.
			 * @param bytes			Byte-array
			 * @param step			Pixel step size
			 * @throws ArrayIndexOutOfBoundsException if the swizzle indices or the step size are invalid for the given array
			 */
			public void apply(byte[] bytes, int step) {
				for(int n = 0; n < bytes.length; n += step) {
					ArrayUtils.swap(bytes, n + src, n + dest);
				}
			}
		}

		/**
		 * Converts the given image byte-array to a buffer and injects the given alpha value.
		 * @param bytes		Image byte array
		 * @param step		Pixel step size
		 * @param alpha		Alpha value
		 * @return Image buffer
		 */
		public static ByteBuffer alpha(byte[] bytes, int step, byte alpha) {
			// Allocate buffer sized to this array plus the alpha component
			final int len = (bytes.length / step) * (step + 1);
			final ByteBuffer bb = BufferFactory.byteBuffer(len);

			// Copy byte array and inject alpha component
			for(int n = 0; n < bytes.length; n += step) {
				for(int c = 0; c < step; ++c) {
					bb.put(bytes[n + c]);
				}
				bb.put(alpha);
			}

			return bb;
		}
	}
}
