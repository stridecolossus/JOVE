package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.range;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.BufferFactory;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Platform-independant image.
 * @author Sarge
 */
public class Image {
	/**
	 * Determines the number of mipmap levels for the given image dimensions.
	 * @param dim Image dimensions
	 * @return Number of mipmap levels
	 */
	public static int levels(Dimensions dim) {
		final float max = Math.max(dim.width(), dim.height());
		return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
	}

	/**
	 * Image component type.
	 */
	public enum Type {
		// TODO - just use classes?
		BYTE(1),
		INT(Integer.BYTES),
		FLOAT(Float.BYTES);

		private final int size;

		private Type(int size) {
			this.size = size;
		}

		/**
		 * @return Size of this component type (bytes)
		 */
		public int size() {
			return size;
		}
	}

	/**
	 * Image format.
	 */
	public static class Format extends AbstractEqualsObject {
		private final int components;
		private final Type type;

		/**
		 * Constructor.
		 * @param components		Number of components 1..4
		 * @param type				Component type
		 */
		protected Format(int components, Type type) {
			this.components = range(components, 1, 4);
			this.type = notNull(type);
		}

		/**
		 * @return Number of components
		 */
		public int components() {
			return components;
		}

		/**
		 * @return Component type
		 */
		public Type type() {
			return type;
		}

		/**
		 * Determines the buffer length of an image of this format with the given dimensions.
		 * @param size Image dimensions
		 * @return Length (bytes)
		 */
		private int length(Dimensions size) {
			return size.width * size.height * components * type.size;
		}
	}

	private final Format format;
	private final Dimensions size;
	private final ByteBuffer buffer;

	/**
	 * Constructor.
	 * @param format	Format descriptor
	 * @param size		Image size
	 * @param buffer	Image data
	 */
	public Image(Format format, Dimensions size, ByteBuffer buffer) {
		this.format = notNull(format);
		this.size = notNull(size);
		this.buffer = notNull(buffer);
		verify();
	}

	private void verify() {
		if(buffer.capacity() != length()) {
			throw new IllegalArgumentException(String.format("Invalid image buffer length: expected=%d actual=%d size=%s format=%s", buffer.capacity(), length(), size, format));
		}
	}

	/**
	 * @return Image format
	 */
	public Format format() {
		return format;
	}

	/**
	 * @return Image dimensions
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * @return Buffer length (bytes)
	 */
	public int length() {
		return format.length(size);
	}

	/**
	 * @return Image data buffer
	 */
	public ByteBuffer buffer() {
		return buffer;
	}

	/**
	 * Loader for a default image.
	 * @see ImageIO
	 */
	public static class Loader {
		// https://stackoverflow.com/questions/41404362/greyscale-texture-format-in-vulkan
		// https://www.baeldung.com/java-images
		// 12 monkeys? extends ImageIO

		/**
		 * Loads an image.
		 * @param in Input stream
		 * @return Image
		 * @throws IOException if the image cannot be loaded
		 */
		public Image load(InputStream in) throws IOException {
			// Load image
			final BufferedImage image = ImageIO.read(in);

			// Determine image format
			final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
			final Format format = format(image);

			// Allocate buffer
			final int len = format.length(dim);
			final ByteBuffer buffer = BufferFactory.byteBuffer(len);

			// Copy image to buffer
			final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
			buffer(data, image.getType(), buffer);
			buffer.flip();

			// Create image
			return new Image(format, dim, buffer);
		}

		/**
		 * Copies image data to the given buffer.
		 * @param data			Image data
		 * @param type			Image type
		 * @param buffer		Buffer
		 */
		private void buffer(DataBufferByte data, int type, ByteBuffer buffer) {
			// TODO - needs tidying up and support for other formats (and int, float)
			final byte[] bytes = data.getData();
			switch(type) {
			case BufferedImage.TYPE_3BYTE_BGR:
				for(int n = 0; n < bytes.length; n += 3) {
					buffer.put(bytes[n+2]);
					buffer.put(bytes[n+1]);
					buffer.put(bytes[n]);
					buffer.put(Byte.MAX_VALUE);
				}
				break;

			case BufferedImage.TYPE_4BYTE_ABGR:
				buffer.put(bytes);
				break;

			case BufferedImage.TYPE_BYTE_GRAY:
			case BufferedImage.TYPE_BYTE_INDEXED:
				// TODO - test these properly
				for(int n = 0; n < bytes.length; n += 3) {
					buffer.put(bytes[n]);
				}
				break;

			default:
				throw new UnsupportedOperationException("Unsupported image format: " + this);
			}
		}

		/**
		 * Determines the image format.
		 * @param image Image
		 * @return Image format
		 */
		private static Format format(BufferedImage image) {
			// TODO - others, depends on what VK formats are supported
			switch(image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:		return new Format(4, Type.BYTE);
			case BufferedImage.TYPE_4BYTE_ABGR:		return new Format(4, Type.BYTE);
			case BufferedImage.TYPE_BYTE_GRAY:		return new Format(1, Type.BYTE);
			case BufferedImage.TYPE_BYTE_INDEXED:	return new Format(1, Type.BYTE);
			default:								throw new IllegalArgumentException("Unsupported image format: " + image);
			}
		}
	}
}
