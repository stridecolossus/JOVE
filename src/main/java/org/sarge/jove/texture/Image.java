package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Dimensions;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * Generic image.
 * @author Sarge
 */
public interface Image {
	/**
	 * Image formats.
	 */
	enum Format {
		RGB,
		RGBA,
		GRAY_SCALE;

		/**
		 * @return Whether this format is translucent (has an alpha channel)
		 */
		public boolean isTranslucent() {
			return this == RGBA;
		}
	}

	/**
	 * Image header.
	 */
	class Header extends AbstractEqualsObject {
		private final Format format;
		private final Dimensions size;
		// TODO - component size? compressed?

		/**
		 * Constructor.
		 * @param format		Image format
		 * @param size			Dimensions
		 */
		public Header(Format format, Dimensions size) {
			this.format = notNull(format);
			this.size = notNull(size);
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
	}

	/**
	 * Determines the number of mipmap levels for the given image dimensions.
	 * @param dim Image dimensions
	 * @return Number of mipmap levels
	 */
	static int levels(Dimensions dim) {
		final float max = Math.max(dim.width(), dim.height());
		return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
	}

	/**
	 * @return Image header
	 */
	Header header();

	/**
	 * Looks up the pixel at the given coordinates.
	 * @param x
	 * @param y
	 * @return Pixel
	 */
	int pixel(int x, int y);

	/**
	 * @return Data buffer for this image
	 */
	ByteBuffer buffer();

	/**
	 * Converts this image.
	 * @param header Target image header
	 * @return Converted image
	 */
	Image convert(Header header);
}
