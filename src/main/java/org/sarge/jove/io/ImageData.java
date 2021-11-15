package org.sarge.jove.io;

import static java.util.stream.Collectors.toList;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.lib.util.Check;

/**
 * An <i>image data</i> is an abstraction for a general image.
 * <p>
 * The {@link #count} is the number of images (or array layers).  An image array can be created by the {@link #array(List)} factory method.
 * <p>
 * The image {@link #layout} specifies the number of channels comprising the image and the structure of each pixel.
 * For example a standard ABGR image with one byte per channel would have the following layout: <code>new Layout("ABGR", Byte.class, 1, false)</code>
 * <p>
 * TODO - MIP levels
 * @author Sarge
 */
public record ImageData(Dimensions size, int count, int mip, Layout layout, Bufferable data) {
	/**
	 * Constructor.
	 * @param size			Image dimensions
	 * @param count			Number of images/layers
	 * @param mip			MIP levels
	 * @param layout		Component layout
	 * @param data			Image data
	 * @throws IllegalArgumentException if the data length is not valid for the image
	 */
	public ImageData {
		Check.notNull(size);
		Check.oneOrMore(count);
		Check.oneOrMore(mip);
		Check.notNull(layout);
		Check.notNull(data);
		final int expected = offset(count, size, layout);
		if(data.length() != expected) throw new IllegalArgumentException(String.format("Invalid image data length: expected=%d actual=%d", expected, data.length()));
	}

	/**
	 * Copy constructor.
	 * @param image		Image header
	 * @param count		Number of images
	 * @param data		Image data
	 */
	private ImageData(ImageData image, int count, Bufferable data) {
		this(image.size, count, 1, image.layout, data);
	}

	/**
	 * Calculates the buffer offset for the given image index.
	 * @param index Image index
	 * @return Offset
	 * @throws IllegalArgumentException if the given index is larger than the number of images
	 */
	public int offset(int index) {
		Check.zeroOrMore(index);
		if(index >= count) throw new IllegalArgumentException(String.format("Invalid image array index: index=%d this=%s", index, this));
		return offset(index, size, layout);
	}

	/**
	 * Helper - Calculates the buffer offset.
	 * @param index			Image index
	 * @param size			Dimensions
	 * @param layout		Layout
	 * @return Offset
	 */
	private static int offset(int index, Dimensions size, Layout layout) {
		return index * size.area() * layout.count() * layout.bytes();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("size", size)
				.append("count", count)
				.append("mips", mip)
				.append(layout)
				.build();
	}

	/**
	 * Creates an image array.
	 * @param images Images
	 * @return Image array
	 * @throws IllegalArgumentException unless <b>all</b> the given images have the same header properties
	 */
	public static ImageData array(List<ImageData> images) {
		// Determine number of images
		final int count = images.size();
		if(count == 0) throw new IllegalArgumentException("Image array must contain at least one image");

		// Init header
		final ImageData header = images.get(0);
		final int len = header.data().length() * count;

		// Validates images
		images
				.stream()
				.skip(1)
				.forEach(e -> validate(header, e));

		// Build compound image data
		final List<Bufferable> compound = images
				.stream()
				.map(ImageData::data)
				.collect(toList());

		// Create compound bufferable
		final Bufferable data = new Bufferable() {
			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				for(Bufferable b : compound) {
					b.buffer(bb);
				}
			}
		};

		// Create image array
		return new ImageData(header, count, data);
	}

	/**
	 * Validates an image array element.
	 */
	private static void validate(ImageData header, ImageData image) {
		// Check not already an image array
		if(image.count > 1) throw new IllegalArgumentException("Already an image array: " + image);

		// Compare image headers
		final boolean matched =
				image.size().equals(header.size()) &&
				(image.mip() == header.mip()) &&
				image.layout().equals(header.layout());

		if(!matched) {
			throw new IllegalArgumentException(String.format("Mismatched image array: expected=%s actual=%s", header, image));
		}

		assert image.data().length() == header.data().length();
	}
}
