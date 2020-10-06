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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.Check;
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
	 * @return Component sizes
	 */
	List<Integer> components();

	/**
	 * @return Image data
	 */
	ByteBuffer buffer();

	/**
	 * Default implementation.
	 */
	class DefaultImageData implements ImageData {
		private final Dimensions size;
		private final List<Integer> components;
		private final ByteBuffer data;

		/**
		 * Constructor.
		 * @param size				Dimensions
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
		public ByteBuffer buffer() {
			return data.flip().asReadOnlyBuffer();
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
	 * Image data transform.
	 */
	public interface Transform {
		/**
		 * Applies this transform to the given image data.
		 * @param bytes		Image data
		 * @param step		Pixel step size
		 */
		void transform(byte[] bytes, int step);
	}

	/**
	 * An <i>image swizzle</i> is used to swap components of an image byte array.
	 * <p>
	 * For example, to transform a BGR image to RGB:
	 * <pre>
	 *  byte[] bytes = ...
	 *  Swizzle swizzle = new Swizzle(0, 2);		// Swap the R and G components
	 *  swizzle.transform(bytes, 3);				// Apply to 3-component sized image
	 * </pre>
	 */
	public record Swizzle(int src, int dest) implements Transform {
		@Override
		public void transform(byte[] bytes, int step) {
			for(int n = 0; n < bytes.length; n += step) {
				ArrayUtils.swap(bytes, n + src, n + dest);
			}
		}
	}

	/**
	 * Image loader implemented using {@link ImageIO} and {@link BufferedImage}.
	 */
	public static class Loader {
		/**
		 * Converter entry.
		 */
		private static record Entry(Integer alpha, int[] components, Transform[] transforms) {
			// Record
		}

		private final DataSource src;
		private final Map<Integer, Entry> converters = new HashMap<>();

		/**
		 * Constructor.
		 * @param src Data source
		 */
		public Loader(DataSource src) {
			this.src = notNull(src);
			init();
		}

		/**
		 * Registers built-in image converters.
		 */
		private void init() {
			add(BufferedImage.TYPE_BYTE_INDEXED, 	1, null);
			add(BufferedImage.TYPE_3BYTE_BGR, 		4, BufferedImage.TYPE_4BYTE_ABGR, new Swizzle(0, 2));
			add(BufferedImage.TYPE_4BYTE_ABGR, 		4, null, new Swizzle(0, 3), new Swizzle(1, 2));
		}

		/**
		 * Registers an image converter.
		 * @param type				Buffered image type
		 * @param components		Output component specification
		 * @param alpha				Buffered image type to attach alpha channel or {@code null} if not required
		 * @param transforms		Additional image data transforms
		 */
		public void add(int type, int components, Integer alpha, Transform... transforms) {
			Check.oneOrMore(components);
			Check.notNull(transforms);
			converters.put(type, new Entry(alpha, new int[components], transforms));
		}

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
			final var entry = converters.get(image.getType());
			if(entry == null) throw new IOException("Unsupported image type: " + image);

			// Add alpha channel as required
			final BufferedImage result = addAlpha(image, entry.alpha);

			// Apply transforms
			// TODO - handle other buffer types (int, etc)?
			final DataBufferByte buffer = (DataBufferByte) result.getRaster().getDataBuffer();
			final byte[] bytes = buffer.getData();
			for(Transform t : entry.transforms) {
				t.transform(bytes, entry.components.length);
			}

			// Convert to buffer
			final ByteBuffer bb = ByteBuffer.wrap(bytes);

			// Create image wrapper
			final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
			return new DefaultImageData(dim, entry.components, bb);
		}

		/**
		 * Adds an alpha channel to a buffered image.
		 * @param image		Original image
		 * @param type		Buffered image type with alpha channel or {@code null} if not required
		 * @return Image with alpha channel
		 */
		private static BufferedImage addAlpha(BufferedImage image, Integer type) {
			// Ignore if already has alpha
			if(type == null) {
				return image;
			}

			// Otherwise add alpha channel
			final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
			final Graphics g = result.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			return result;
		}
	}
}
