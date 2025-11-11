package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.List;

import org.sarge.jove.common.*;

/**
 * An <i>image data</i> is a wrapper for a general image comprising multiple array layers and MIP levels.
 * <p>
 * The image {@link #channels} specifies the order of the channels comprising the image, e.g. {@code ABGR} for a transparent native image.
 * <p>
 * The {@link #layout} describes the pixel structure of each channel.
 * For example an RGB image with one byte per channel would have the following layout: <code>new Component(3, Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public class ImageData {		// TODO - record
	/**
	 * An <i>image level</i> specifies the MIP levels of this image.
	 */
	public record Level(int offset, int length) {
		/**
		 * Constructor.
		 * @param offset		Offset into the image data
		 * @param length		Length of this level
		 */
		public Level {
			requireZeroOrMore(offset);
			requireOneOrMore(length);
		}

		/**
		 * Helper - Calculates the offset into the image data for the given layer.
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
			return 1 + (int) Math.floor(Math.log(max) / Math.log(2));		// TODO - explanation
		}
	}

	private final Dimensions size;
	private final String channels;
	private final Layout layout;
	private final byte[] data;

	/**
	 * Constructor.
	 * @param size				Image dimensions
	 * @param channels			Channels
	 * @param layout			Pixel layout
	 * @param data				Image data
	 * @throws IllegalArgumentException for a {@link #channel} that is not one of RGBA (upper case sensitive)
	 * @throws IllegalArgumentException if the number of {@link #channels} and the {@link #layout} do not match
	 * @throws IllegalArgumentException if the length of the {@link #data} does not match the number of MIP levels and array layers
	 */
	public ImageData(Dimensions size, String channels, Layout layout, byte[] data) {
		this.size = requireNonNull(size);
		this.channels = requireNotEmpty(channels);
		this.layout = requireNonNull(layout);
		this.data = requireNonNull(data);
		validate();
	}

	private void validate() {
		// Validate channels
		for(char ch : channels.toCharArray()) {
			if(Colour.RGBA.indexOf(ch) == -1) throw new IllegalArgumentException("Invalid channel character: " + ch);
		}

		// Check channels and layout match
		if(channels.length() != layout.count()) {
			throw new IllegalArgumentException("Mismatched image channels and layout: components=%s layout=%s".formatted(channels, layout));
		}

		// Check overall image buffer matches MIP levels
		final int total = this.levels().stream().mapToInt(Level::length).sum();
		if(data.length != total) {
			throw new IllegalArgumentException("Invalid image data length: expected=%d actual=%d".formatted(total, data.length));
		}
	}

	/**
	 * @return Image dimensions
	 */
	public final Dimensions size() {
		return size;
	}

	/**
	 * @return Image depth
	 */
	public int depth() {
		return 1;
	}

	/**
	 * @return Image channels
	 */
	public final String channels() {
		return channels;
	}

	/**
	 * @return Pixel layout
	 */
	public final Layout layout() {
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

// TODO
//	/**
//	 * @return Image data
//	 */
//	public final ByteSizedBufferable data() {
//		return ByteSizedBufferable.of(data);
//	}
	public byte[] data() {
		return data;
	}

	/**
	 * Retrieves the pixel at the given image coordinates (sampled from the <b>first</b> layer and MIP level).
	 * @param component RGBA component index
	 * @return Pixel
	 * @throws ArrayIndexOutOfBoundsException if the coordinates are invalid for this image
	 * @throws IllegalArgumentException if the component index is invalid for this image
	 * @see #pixel(int)
	 */
	public final int pixel(int x, int y, int component) {
		if((component < 0) || (component >= channels.length())) throw new IllegalArgumentException("Invalid component index: component=%d channels=%d".formatted(component, channels.length()));
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
}
