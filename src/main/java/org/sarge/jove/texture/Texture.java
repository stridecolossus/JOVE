package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.range;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;

/**
 * A <i>texture</i> is an image or data array uploaded to the graphics system.
 * @author Sarge
 */
public final class Texture {
	/**
	 * A <i>texture descriptor</i> specifies the render properties of a texture.
	 */
	public static final class Descriptor {
		/**
		 * Texture wrapping policy.
		 */
		public enum Wrap {
			/**
			 * Coordinates wrap around the texture.
			 */
			REPEAT,

			/**
			 * Coordinates are <i>reflected</i>.
			 */
			MIRROR,

			/**
			 * Coordinates are clamped to 0..1.
			 */
			CLAMP,
		}

		/**
		 * Texture filtering.
		 */
		public enum Filter {
			LINEAR,
			NEAREST
		}

		private final Image.Header header;
		private final int dim;
		private final Wrap wrap;
		private final int levels;
		private final Filter min;
		private final Filter mag;
		private final Filter mipmap;

		// TODO
		// - mipmap base/max level
		// - mipmap LOD - min/max/bias
		// - mipmap filter (combined with minification -> opengl filter)
		// - ansitropic filtering (float)?
		// - GL_CLAMP_TO_BORDER and border colour?

		/**
		 * Constructor.
		 * @param header		Image header
		 * @param dim			Number of dimensions 1..3
		 * @param wrap			Wrap policy
		 * @param levels		Number of mipmap levels 1..n
		 * @param min			Minification filter
		 * @param mag			Magnification filter
		 * @param mipmap		Mipmap filter
		 */
		private Descriptor(Image.Header header, int dim, Wrap wrap, int levels, Filter min, Filter mag, Filter mipmap) {
			this.header = notNull(header);
			this.dim = range(dim, 1, 3);
			this.wrap = notNull(wrap);
			this.levels = oneOrMore(levels);
			this.min = notNull(min);
			this.mag = notNull(mag);
			this.mipmap = notNull(mipmap);
		}

		/**
		 * @return Image header
		 */
		public Image.Header header() {
			return header;
		}

		/**
		 * @return Number of dimensions 1..3
		 */
		public int dimensions() {
			return dim;
		}

		/**
		 * @return Wrapping policy
		 */
		public Wrap wrap() {
			return wrap;
		}

		/**
		 * @return Number of mipmap levels 1..n
		 */
		public int levels() {
			return levels;
		}

		/**
		 * @return Minification filter
		 */
		public Filter min() {
			return min;
		}

		/**
		 * @return Magnification filter
		 */
		public Filter mag() {
			return mag;
		}

		/**
		 * @return Mipmap filter
		 */
		public Filter mipmap() {
			return mipmap;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}

		/**
		 * Builder for a texture descriptor.
		 */
		public static class Builder {
			private Image.Header image;
			private int dim = 2;
			private Wrap wrap = Wrap.REPEAT;
			private Integer levels;
			private Filter min = Filter.LINEAR;
			private Filter mag = Filter.LINEAR;
			private Filter mipmap = Filter.LINEAR;

			/**
			 * Sets the image descriptor of this texture.
			 * @param image Image descriptor
			 */
			public Builder header(Image.Header image) {
				this.image = notNull(image);
				return this;
			}

			/**
			 * Sets the dimension of this texture.
			 * @param dim Number of dimensions 1..3
			 */
			public Builder dimension(int dim) {
				this.dim = notNull(dim);
				return this;
			}

			/**
			 * Sets the wrapping policy of this texture.
			 * @param wrap Wrapping policy
			 */
			public Builder wrap(Wrap wrap) {
				this.wrap = notNull(wrap);
				return this;
			}

			/**
			 * Sets the number of mipmap levels.
			 * If this property is not explicitly set the number of levels is defaulted to {@link Texture#levels(Dimensions)}.
			 * @param levels Number of mipmap levels
			 */
			public Builder levels(int levels) {
				this.levels = oneOrMore(levels);
				return this;
			}

			/**
			 * Sets the minification filter of this texture.
			 * @param min Minification filter
			 */
			public Builder min(Filter min) {
				this.min = notNull(min);
				return this;
			}

			/**
			 * Sets the magnification filter of this texture.
			 * @param mag Magnification filter
			 */
			public Builder mag(Filter mag) {
				this.mag = notNull(mag);
				return this;
			}

			/**
			 * Sets the mipmap minification filter.
			 * @param mipmap Mipmap filter
			 */
			public Builder mipmap(Filter mipmap) {
				this.mipmap = notNull(mipmap);
				return this;
			}

			/**
			 * Constructs this texture descriptor.
			 * @return New descriptor
			 */
			public Descriptor build() {
				if(levels == null) {
					levels = Texture.levels(image.size());
				}
				return new Descriptor(image, dim, wrap, levels, min, mag, mipmap);
			}
		}
	}

	/**
	 * Determines the number of mipmap levels for the given image dimensions.
	 * @param dim Image dimensions
	 * @return Number of mipmap levels
	 */
	public static int levels(Dimensions dim) {
		final float max = Math.max(dim.width(), dim.height());
		return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
	}

	// TODO - scale dimensions by mipmap level -> texture loader

	private final Descriptor descriptor;

	/**
	 * Constructor.
	 * @param descriptor Texture descriptor
	 */
	public Texture(Descriptor descriptor) {
		this.descriptor = notNull(descriptor);
	}

	/**
	 * @return Texture descriptor
	 */
	public Descriptor descriptor() {
		return descriptor;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
