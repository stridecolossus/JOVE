package org.sarge.jove.io;

import static org.sarge.lib.util.Check.*;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.lib.util.Check;

/**
 * An <i>image data</i> is an abstraction for a general image comprising multiple array layers and MIP levels.
 * <p>
 * The image {@link #components} specifies the order of the channels comprising the image, e.g. {@code ABGR} for a transparent native image.
 * <p>
 * The {@link #layout} describes the pixel structure of each channel.
 * For example an RGB image with one byte per channel would have the following layout: <code>new Layout(3, Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public class ImageData {
	/**
	 * An <i>image level</i> specifies a MIP level of this image.
	 */
	public record Level(int offset, int length) {
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

	private final Dimensions size;
	private final String components;
	private final Component layout;
	private final byte[] data;

	/**
	 * Constructor.
	 * @param size				Image dimensions
	 * @param components		Channel components
	 * @param layout			Pixel layout
	 * @param data				Image data
	 * @throws IllegalArgumentException if the size of the components and layout do not match
	 * @throws IllegalArgumentException if the image data does not match the number of MIP levels and array layers
	 */
	protected ImageData(Dimensions size, String components, Component layout, byte[] data) {
		this.size = notNull(size);
		this.components = notEmpty(components);
		this.layout = notNull(layout);
		this.data = notNull(data);
		validate();
	}

	private void validate() {
		// Check number of components and layout match
		if(components.length() != layout.count()) {
			throw new IllegalArgumentException(String.format("Mismatched image components and layout: components=%s layout=%s", components, layout));
		}

		// Check overall image buffer matches MIP levels
		final int total = this.levels().stream().mapToInt(Level::length).sum();
		if(data.length != total) {
			throw new IllegalArgumentException(String.format("Invalid image data length: expected=%d actual=%d", total, data.length));
		}
	}

	/**
	 * @return Image dimensions
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * @return Image depth
	 */
	public int depth() {
		return 1;
	}

	/**
	 * @return Pixel components
	 */
	public String components() {
		return components;
	}

	/**
	 * @return Pixel layout
	 */
	public Component layout() {
		return layout;
	}

	/**
	 * @return Vulkan format hint
	 */
	public int format() {
		return 0;
	}

	/**
	 * @return MIP levels
	 */
	public List<Level> levels() {
		return List.of(new Level(0, data.length));
	}

	/**
	 * @return Number of array layers
	 */
	public int layers() {
		return 1;
	}

	/**
	 * @return Image data
	 */
	public ByteSizedBufferable data() {
		return ByteSizedBufferable.of(data);
	}

	/**
	 * Retrieves the pixel at the given image coordinates (sampled from the <b>first</b> layer and MIP level).
	 * @param component RGBA component index
	 * @return Pixel
	 * @throws ArrayIndexOutOfBoundsException if the coordinates are invalid for this image
	 * @throws IllegalArgumentException if the component index is invalid for this image
	 * @see #pixel(int)
	 */
	public int pixel(int x, int y, int component) {
		Check.range(component, 0, components.length() - 1);
		final int offset = levels().get(0).offset;
		final int start = (x + y * size.width()) * layout.stride();
		final int index = offset + start + (component * layout.bytes());
		return pixel(index);
	}

	/**
	 * Retrieves the pixel at the given byte index.
	 * @param index Byte index
	 * @return Pixel
	 * @throws ArrayIndexOutOfBoundsException if the index is invalid for this image
	 */
	protected int pixel(int index) {
		return data[index];
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(size)
				.append("depth", depth())
				.append(components)
				.append(layout)
				.append("levels", levels().size())
				.append("layers", layers())
				.append("format", format())
				.build();
	}
}
