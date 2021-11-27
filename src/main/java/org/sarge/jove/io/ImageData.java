package org.sarge.jove.io;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.lib.util.Check;

/**
 * An <i>image data</i> is an abstraction for a general image comprising multiple array layers and MIP levels.
 * <p>
 * The image {@link #components} specifies the order of the channels comprising the image, e.g. {@code ABGR} for a transparent native image.
 * <p>
 * The {@link #layout} describes the number of channels comprising the image and the structure of each pixel.
 * For example a transparent image with one byte per channel would have the following layout: <code>new Layout(4, Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image extents
	 */
	Extents extents();

	/**
	 * @return Components
	 */
	String components();

	/**
	 * @return Component layout
	 */
	Layout layout();

	/**
	 * @return Format hint
	 */
	int format();

	/**
	 * @return Number of layers
	 */
	int layers();

	/**
	 * @return MIP levels
	 */
	List<Level> levels();

	/**
	 * Retrieves the image data for the given layer.
	 * @param layer Image layer
	 * @return Image data
	 * @throws IndexOutOfBoundsException for an invalid layer index
	 */
	Bufferable data(int layer);

	/**
	 * An <i>image level</i> specifies a MIP level of this image.
	 * @see Extents#mip(int)
	 */
	record Level(int offset, int length) {
		/**
		 * Constructor.
		 * @param offset		Offset into the image data
		 * @param length		Length of this level
		 */
		public Level {
			Check.zeroOrMore(offset);
			Check.oneOrMore(length);
		}

		/**
		 * Helper - Determines the number of mipmap levels for the given image dimensions.
		 * @param dim Image dimensions
		 * @return Number of mipmap levels
		 */
		public static int levels(Dimensions dim) {
			final float max = Math.max(dim.width(), dim.height());
			return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
		}
	}

	/**
	 * Extents of this image.
	 */
	record Extents(Dimensions size, int depth) {
		/**
		 * Constructor.
		 * @param dim		Image dimensions
		 * @param depth		Depth
		 */
		public Extents {
			Check.notNull(size);
			Check.zeroOrMore(depth);
		}

		/**
		 * Convenience constructor for a 2D image.
		 */
		public Extents(Dimensions size) {
			this(size, 1);
		}

		/**
		 * Calculates the image extents for the given MIP level.
		 * @param level MIP level
		 * @return MIP extents
		 */
		public Extents mip(int level) {
			Check.zeroOrMore(level);

			if(level == 0) {
				return this;
			}

			final int w = mip(size.width(), level);
			final int h = mip(size.height(), level);
			return new Extents(new Dimensions(w, h), depth);
		}

		private static int mip(int value, int level) {
			return Math.max(1, value >> level);
		}
	}

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractImageData implements ImageData {
		private final Extents extents;
		private final String components;
		private final Layout layout;
		private final int format;
		private final List<Level> levels;

		/**
		 * Constructor.
		 * @param size				Image extents
		 * @param components		Components
		 * @param layout			Layout
		 * @param format			Format hint
		 * @param levels			MIP levels
		 * @throws IllegalArgumentException if the size of the layout does not match the number of components
		 */
		protected AbstractImageData(Extents extents, String components, Layout layout, int format, List<Level> levels) {
			this.extents = notNull(extents);
			this.components = notEmpty(components);
			this.layout = notNull(layout);
			this.format = zeroOrMore(format);
			this.levels = List.copyOf(levels);
			validate();
		}

		private void validate() {
			if(components.length() != layout.size()) {
				throw new IllegalArgumentException(String.format("Mismatched image components and layout: components=%s layout=%s", components, layout));
			}
		}

		@Override
		public Extents extents() {
			return extents;
		}

		@Override
		public String components() {
			return components;
		}

		@Override
		public Layout layout() {
			return layout;
		}

		@Override
		public int format() {
			return format;
		}

		@Override
		public List<Level> levels() {
			return levels;
		}

		@Override
		public int layers() {
			return 1;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(extents)
					.append(components)
					.append(layout)
					.append("format", format)
					.append("levels", levels.size())
					.append("layers", layers())
					.build();
		}
	}
}
