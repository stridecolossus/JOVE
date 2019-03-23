package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.BufferFactory;

/**
 * Default image implemented using AWT.
 * @author Sarge
 * TODO - Windows/AWT only
 */
public class DefaultImage implements Image {
	private final Header header;
	private final BufferedImage image;

	/**
	 * Constructor.
	 * @param image Image
	 */
	public DefaultImage(BufferedImage image) {
		final Dimensions size = new Dimensions(image.getWidth(), image.getHeight());
		final Format format = map(image.getColorModel());
		this.header = new Header(format, size);
		this.image = notNull(image);
	}

	/**
	 * Constructor.
	 * @param header	Image header
	 * @param image		Image
	 */
	private DefaultImage(Header header, BufferedImage image) {
		this.header = notNull(header);
		this.image = notNull(image);
	}

	/**
	 * Maps the given colour model to the image format.
	 * @param model Colour model
	 * @return Image format
	 */
	private static Format map(ColorModel model) {
		final int size = model.getColorSpace().getNumComponents();
		if(size == 1) {
			return Format.GRAY_SCALE;
		}
		else
		if(size == 3) {
			if(model.hasAlpha()) {
				return Format.RGBA;
			}
			else {
				return Format.RGB;
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported colour model: " + model);
		}
	}

	@Override
	public Header header() {
		return header;
	}

	@Override
	public int pixel(int x, int y) {
		return image.getRGB(x, y);
	}

	@Override
	public ByteBuffer buffer() {
		// TODO - helper?
		// TODO - do all image types automatically do this or do we need to convert first?
		final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
		final ByteBuffer buffer = BufferFactory.byteBuffer(data.getSize());
		buffer.put(data.getData());
		buffer.flip();
		return buffer;
	}

	@Override
	public Image convert(Header header) {
		final Dimensions size = header.size();
		return convert(header, size.width(), size.height());
	}

	public Image convert(Header descriptor, int w, int h) {
		// Create target image
		final int type = map(descriptor.format());
		final Dimensions size = descriptor.size();
		final BufferedImage result = new BufferedImage(size.width(), size.height(), type);

		// Draw image to target
		final Graphics g = result.getGraphics();
		g.drawImage(image, 0, 0, size.width(), size.height(), 0, 0, w, h, null);
		g.dispose();

		// Create new image wrapper
		return new DefaultImage(descriptor, result);
	}

	private static int map(Format format) {
		switch(format) {
		case RGB:			return BufferedImage.TYPE_3BYTE_BGR;
		case RGBA:			return BufferedImage.TYPE_4BYTE_ABGR;
		case GRAY_SCALE:	return BufferedImage.TYPE_BYTE_GRAY;
		default:			throw new RuntimeException();
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Loader for a default image.
	 * @see ImageIO
	 * TODO - this should be a platform-specific service
	 */
	public static class Loader {
		private boolean flip;

		/**
		 * Sets whether to flip images in the Y direction.
		 * @param flip Whether to flip
		 */
		public void flip(boolean flip) {
			this.flip = flip;
		}

		/**
		 * Loads an image.
		 * @param in Input stream
		 * @return Image
		 * @throws IOException if the image cannot be loaded
		 */
		public Image load(InputStream in) throws IOException {
			// Load image
			final DefaultImage image = new DefaultImage(ImageIO.read(in));

			// Flip as required
			if(flip) {
				final Dimensions size = image.header.size();
				return image.convert(image.header, size.height(), size.width());
			}
			else {
				return image;
			}
		}
	}
}
