package org.sarge.jove.io;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.LittleEndianDataInputStream;
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
	 * @return Pixel components
	 */
	String components();

	/**
	 * @return Pixel layout
	 */
	Layout layout();

	/**
	 * @return Vulkan format hint
	 */
	int format();

	/**
	 * @return MIP levels
	 */
	List<Level> levels();

	/**
	 * @return Number of image layers
	 */
	int layers();

	/**
	 * @return Image data
	 */
	Bufferable data();

	/**
	 * Retrieves the pixel at the given image coordinates.
	 * <p>
	 * Note that the pixel is sampled from the <b>first</b> layer and MIP level.
	 * <p>
	 * @param component RGBA component index
	 * @return Pixel
	 * @throws ArrayIndexOutOfBoundsException if the coordinates are invalid for this image
	 * @throws IllegalArgumentException if the component index is invalid for this image
	 */
	int pixel(int x, int y, int component);

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
		 * Helper - Calculate the offset into the image data for the given layer.
		 * @param layer			Layer index
		 * @param count			Number of layers
		 * @return Offset
		 * @throws IllegalArgumentException if the layer index is larger than the given number of layers
		 */
		public int offset(int layer, int count) {
			if(layer >= count) throw new IllegalArgumentException(String.format("Illogical offset for layer %d/%d", layer, count));
			return offset + layer * (length / count);
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
	 * Default implementation.
	 */
	record DefaultImageData(Extents extents, String components, Layout layout, int format, List<Level> levels, int layers, byte[] image) implements ImageData {
		/**
		 * Constructor.
		 * @param extents			Image extents
		 * @param components		Components
		 * @param layout			Layout descriptor
		 * @param format			Vulkan format hint
		 * @param levels			MIP levels
		 * @param layers			Number of array layers
		 * @param image				Image data
		 * @throws IllegalArgumentException if the size of the components and layout do not match
		 * @throws IllegalArgumentException if the length of the data buffer does not match the image extents
		 */
		public DefaultImageData {
			Check.notNull(extents);
			Check.notEmpty(components);
			Check.notNull(layout);
			Check.notEmpty(levels);
			Check.oneOrMore(layers);
			Check.notNull(image);
			levels = List.copyOf(levels);

			if(components.length() != layout.size()) {
				throw new IllegalArgumentException(String.format("Mismatched image components and layout: components=%s layout=%s", components, layout));
			}

//			final int expected = extents.size.area() * layout.length();
//			if(levels.get(0).length != expected) {
//				throw new IllegalArgumentException(String.format("Invalid image data length: expected=%d actual=%s", total, image.length));
//			}

			final int total = levels.stream().mapToInt(Level::length).sum();
			if(image.length != total) {
				throw new IllegalArgumentException(String.format("Invalid image data length: expected=%d actual=%s", total, image.length));
			}

			// TODO - validate levels? or at least < biggest?
		}

		@Override
		public Bufferable data() {
			return Bufferable.of(image);
		}

		@Override
		public int pixel(int x, int y, int component) {
			Check.range(component, 0, components.length() - 1);
			final int offset = levels.get(0).offset;
			final int start = (x + y * extents.size.width()) * layout.length();
			final int index = offset + start + (component * layout.bytes());
			return LittleEndianDataInputStream.convert(image, index, layout.bytes());
		}
		// TODO - assumes little endian => convert to interface/skeleton

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(components)
					.append(layout)
					.append("format", format)
					.append(extents)
					.append("levels", levels.size())
					.append("layers", layers)
					.build();
		}
	}
}
