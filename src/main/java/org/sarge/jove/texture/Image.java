package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;

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
	class Header {
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

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
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
