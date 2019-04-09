package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.platform.Resource;
import org.sarge.lib.util.AbstractEqualsObject;

/**
 * A <i>sampler</i> is used by shaders to sample from a texture.
 * @author Sarge
 */
public interface Sampler extends Resource {
	/**
	 * A <i>sampler descriptor</i> specifies the properties of a texture sampler.
	 */
	public static final class Descriptor extends AbstractEqualsObject {
		/**
		 * Default sampler descriptor.
		 */
		public static final Descriptor DEFAULT = new Builder().build();

		/**
		 * Wrapping policy.
		 */
		public enum Wrap {
			/**
			 * Coordinates wrap around the texture.
			 */
			REPEAT,

			/**
			 * Coordinates are clamped to the edge of the texture.
			 */
			EDGE,

			/**
			 * Coordinates are clamped to the border colour.
			 */
			BORDER,
		}

		/**
		 * Texture filtering.
		 */
		public enum Filter {
			LINEAR,
			NEAREST
		}

		/**
		 * Texture border colour.
		 */
		public enum Border {
			BLACK,
			WHITE,
			TRANSPARENT
		}

		// Filtering
		private final Filter min;
		private final Filter mag;
		private final int anisotrophy;

		// Texture coordinate wrapping policy
		private final Wrap wrap;
		private final boolean mirrored;
		private final Border border;

		// Mip-map filtering
		private final Filter mipmap;
		// TODO
//		info.mipLodBias = 0;
//		info.minLod = 0;
//		info.maxLod = 0;

		/**
		 * Constructor.
		 * @param min			Minification filter
		 * @param mag			Magnification filter
		 * @param anisotrophy	Number of anisotrophy samples (0..16, zero indicates no anisotrophy filtering)
		 * @param wrap			Wrap policy
		 * @param mirrored		Whether texture coordinates are mirrored
		 * @param border		Texture border colour
		 * @param mipmap		Mipmap filter
		 * @throws IllegalArgumentException if the descriptor is invalid
		 */
		public Descriptor(Filter min, Filter mag, int anisotrophy, Wrap wrap, boolean mirrored, Border border, Filter mipmap) {
			this.min = notNull(min);
			this.mag = notNull(mag);
			this.anisotrophy = zeroOrMore(anisotrophy);
			this.wrap = notNull(wrap);
			this.mirrored = mirrored;
			this.border = notNull(border);
			this.mipmap = notNull(mipmap);
			verify();
		}

		/**
		 * @throws IllegalArgumentException if the descriptor is invalid
		 */
		private void verify() {
			if(mirrored && (wrap == Wrap.BORDER)) throw new IllegalArgumentException("Cannot mirror a clamp-to-border wrpaping policy");
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
		 * @return Number of anisotrophy samples (zero indicates no filtering)
		 */
		public int anisotrophy() {
			return anisotrophy;
		}

		/**
		 * @return Wrapping policy
		 */
		public Wrap wrap() {
			return wrap;
		}

		/**
		 * @return Whether texture coordinates are mirrored
		 */
		public boolean isMirrored() {
			return mirrored;
		}

		/**
		 * @return Border colour
		 */
		public Border border() {
			return border;
		}

		/**
		 * @return Mipmap filter
		 */
		public Filter mipmap() {
			return mipmap;
		}

		/**
		 * Builder for a texture descriptor.
		 */
		public static class Builder {
			private Filter min = Filter.LINEAR;
			private Filter mag = Filter.LINEAR;
			private int anisotrophy;

			private Wrap wrap = Wrap.REPEAT;
			private boolean mirrored;
			private Border border = Border.BLACK;

			private Filter mipmap = Filter.LINEAR;

			/**
			 * Sets the minification filter.
			 * @param min Minification filter
			 */
			public Builder min(Filter min) {
				this.min = notNull(min);
				return this;
			}

			/**
			 * Sets the magnification filter.
			 * @param mag Magnification filter
			 */
			public Builder mag(Filter mag) {
				this.mag = notNull(mag);
				return this;
			}

			/**
			 * Sets the number of anisotrophy samples.
			 * @param anisotrophy Anisotrophy samples
			 */
			public Builder anisotrophy(int anisotrophy) {
				this.anisotrophy = anisotrophy;
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
			 * Sets the wrapping policy as mirrored.
			 */
			public Builder mirrored() {
				this.mirrored = true;
				return this;
			}

			/**
			 * Sets the texture border colour.
			 * @param border Border colour
			 */
			public Builder border(Border border) {
				this.border = notNull(border);
				return this;
			}

			/**
			 * Sets the mipmap filter.
			 * @param mipmap Mipmap filter
			 */
			public Builder mipmap(Filter mipmap) {
				this.mipmap = notNull(mipmap);
				return this;
			}

			/**
			 * Constructs this sampler descriptor.
			 * @return New sampler descriptor
			 */
			public Descriptor build() {
				return new Descriptor(min, mag, anisotrophy, wrap, mirrored, border, mipmap);
			}
		}
	}
}
