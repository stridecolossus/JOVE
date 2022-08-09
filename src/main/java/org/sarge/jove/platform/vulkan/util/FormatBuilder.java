package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.*;

import java.util.Map;

import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * The <i>format builder</i> is used to programatically select a Vulkan format.
 * <p>
 * Finding a {@link VkFormat} can be difficult given the size of the enumeration.
 * However the naming convention is consistent and it is therefore possible to specify the format programatically.
 * <p>
 * The {@link #format(Layout)} convenience method can also be used to determine the format from a vertex {@link Layout}.
 * <p>
 * Examples:
 * <pre>
 * // Construct a format: <code>B16G16R16A16_UNORM</code>
 * VkFormat format = new FormatBuilder()
 *      .components("BGRA")			// BGRA
 *      .bytes(2)					// 16
 *      .signed(false)				// U
 *      .type(Type.NORM)			// NORM
 *      .build();
 *
 * // Determine format from a component layout: <code>R32G32B32_SFLOAT</code>
 * VkFormat point = FormatBuilder.format(Point.LAYOUT);
 * </pre>
 * <p>
 * @see VkFormat
 * @author Sarge
 */
public class FormatBuilder {
	private static final IntegerEnumeration.ReverseMapping<VkFormat> MAPPING = IntegerEnumeration.reverse(VkFormat.class);

	/**
	 * Component data-type.
	 */
	public enum Type {
		INT,
		FLOAT,
		NORM,
		SCALED,
		RGB;

		private static final Map<String, Type> TYPES = Map.of(
				"integer",		INT,
				"int",			INT,
				"short",		INT,
				"float",		FLOAT,
				"byte",			NORM
		);

		/**
		 * Maps the given Java type to the corresponding Vulkan component type.
		 * @param type Type
		 * @return Vulkan type
		 * @throws IllegalArgumentException if the given type is not supported
		 */
		public static Type of(Class<?> type) {
			final Type result = TYPES.get(type.getSimpleName().toLowerCase());
			if(result == null) throw new IllegalArgumentException("Unsupported data type: " + type);
			return result;
		}
	}

	/**
	 * Helper - Determines the format for the given vertex layout.
	 * @param layout Vertex layout
	 * @return Format for the given vertex layout
	 */
	public static VkFormat format(Layout layout) {
		return new FormatBuilder()
				.count(layout.size())
				.bytes(layout.bytes())
				.type(Type.of(layout.type()))
				.signed(layout.signed())
				.build();
	}

	/**
	 * Helper - Determine the format of the given image.
	 * <p>
	 * The image format is determined as follows:
	 * <ol>
	 * <li>Use the {@link ImageData#format()} hint unless this value is {@link VkFormat#UNDEFINED}</li>
	 * <li>Otherwise delegate to {@link #format(Layout)} using the layout of the image</li>
	 * </ol>
	 * <p>
	 * @param image Image
	 * @return Image format
	 * @see ImageData#format()
	 */
	public static VkFormat format(ImageData image) {
		final int format = image.format();
		if(format == VkFormat.UNDEFINED.value()) {
			return format(image.layout());
		}
		else {
			return MAPPING.map(image.format());
		}
	}

	private String components;
	private int count;
	private int bytes = Float.BYTES;
	private Type type = Type.FLOAT;
	private boolean signed = true;

	/**
	 * Constructor.
	 */
	public FormatBuilder() {
		components(Colour.RGBA);
	}

	/**
	 * Sets the colour component template characters, e.g. {@code ARGB}.
	 * @param template Colour component template string
	 * @throws IllegalArgumentException if the given template is empty, contains an invalid character, or is longer than 4 components
	 */
	public FormatBuilder components(String components) {
		if(components.length() > 4) throw new IllegalArgumentException(String.format("Invalid components [%s]", components));
		this.components = notEmpty(components);
		return count(components.length());
	}

	/**
	 * Sets the number of components.
	 * @param count Number of components
	 * @throws IllegalArgumentException if the given count exceeds the components template
	 * @see #components(String)
	 */
	public FormatBuilder count(int count) {
		this.count = Check.range(count, 1, components.length());
		return this;
	}

	/**
	 * Sets the number of bytes per component: 1, 2, 4 or 8.
	 * @param bytes Number of bytes per component (default is <code>4</code>)
	 * @throws IllegalArgumentException if the number of bytes is invalid
	 */
	public FormatBuilder bytes(int bytes) {
		Check.range(bytes, 1, 8);
		if(!MathsUtil.isPowerOfTwo(bytes)) {
			throw new IllegalArgumentException("Unsupported number of component bytes: " + bytes);
		}
		this.bytes = bytes;
		return this;
	}

	/**
	 * Sets whether the data type is signed.
	 * @param signed Whether signed type (default is {@code true})
	 */
	public FormatBuilder signed(boolean signed) {
		this.signed = signed;
		return this;
	}

	/**
	 * Sets the Vulkan data type.
	 * @param type Data type (default is {@link Type#FLOAT})
	 */
	public FormatBuilder type(Type type) {
		this.type = notNull(type);
		return this;
	}

	/**
	 * Builds the format identifier.
	 * @return Vulkan format
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public VkFormat build() {
		// Build component layout
		final StringBuilder layout = new StringBuilder();
		final int size = bytes * Byte.SIZE;
		for(int n = 0; n < count; ++n) {
			layout.append(components.charAt(n));
			layout.append(size);
		}

		// Build format string
		final char ch = signed ? 'S' : 'U';
		final String format = String.format("%s_%c%s", layout, ch, type.name());

		// Lookup format
		return VkFormat.valueOf(format);
	}
}

