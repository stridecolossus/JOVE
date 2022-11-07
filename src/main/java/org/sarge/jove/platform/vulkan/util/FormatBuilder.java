package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.*;

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
 * Generally the identifier for a format is comprised of two elements: the <i>component format</i> and the <i>numeric format</i>.
 * The component format specifies the number of components (or channels) and the size of each in bytes.
 * The numeric format specifies the range of each value and whether it is signed.
 * <p>
 * Example:
 * <pre>
 * VkFormat format = new FormatBuilder()
 *     .components("BGRA")  // BGRA
 *     .bytes(2)            // 16
 *     .signed(false)       // U
 *     .type(Type.NORM)     // NORM
 *     .build();            // B16G16R16A16_UNORM</pre>
 * <p>
 * The {@link #format(ByteSized)} convenience method can also be used to determine the format from a vertex layout:
 * <pre>
 * VkFormat point = FormatBuilder.format(Point.LAYOUT); // R32G32B32_SFLOAT</pre>
 * <p>
 * @see VkFormat
 * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/html/vkspec.html#texel-block-size">Texel block sizes</a>
 * @author Sarge
 */
public class FormatBuilder {
	private static final IntegerEnumeration.ReverseMapping<VkFormat> MAPPING = IntegerEnumeration.reverse(VkFormat.class);

	/**
	 * Vulkan numeric formats.
	 * <p>
	 * The Vulkan numeric formats correspond the general {@link ByteSized.Type} equivalents with the following special cases:
	 * <ul>
	 * <li>{@link #SCALED} are integer values converted to floating-point</li>
	 * <li>{@link #RGB} uses the {@code sRGB} nonlinear encoding</li>
	 * </ul>
	 */
	public enum NumericFormat {
		INT,
		FLOAT,
		NORM,
		SCALED,
		RGB;

		/**
		 * Maps the given layout component type to the corresponding Vulkan numeric format.
		 * @param type Layout component type
		 * @return Vulkan numeric format
		 * @throws UnsupportedOperationException if the given type is not supported
		 */
		public static NumericFormat of(Component.Type type) {
			return switch(type) {
				case INTEGER -> INT;
				case FLOAT -> FLOAT;
				case NORMALIZED -> NORM;
				default -> throw new UnsupportedOperationException("Unsupported component type: " + type);
			};
		}
	}

	/**
	 * Helper - Determines the Vulkan format for the given layout.
	 * @param layout Layout
	 * @return Format for the given layout
	 */
	public static VkFormat format(Component layout) {
		return new FormatBuilder()
				.count(layout.size())
				.bytes(layout.bytes())
				.type(NumericFormat.of(layout.type()))
				.signed(layout.signed())
				.build();
	}

	/**
	 * Helper - Determine the format of the given image.
	 * <p>
	 * The image format is determined as follows:
	 * <ol>
	 * <li>Use the {@link ImageData#format()} hint unless this value is {@link VkFormat#UNDEFINED}</li>
	 * <li>Otherwise delegate to {@link #format(Component)} using the image layout</li>
	 * </ol>
	 * <p>
	 * @param image Image
	 * @return Image format
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
	private NumericFormat numeric = NumericFormat.FLOAT;
	private boolean signed = true;

	/**
	 * Constructor.
	 */
	public FormatBuilder() {
		components(Colour.RGBA);
	}

	/**
	 * Sets the colour components, e.g. {@code RGBA}.
	 * @param template Colour components
	 * @throws IllegalArgumentException if the given components is empty or is longer than 4 components
	 * @see Colour#RGBA
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
	 * Sets the numeric format.
	 * @param numeric Numeric format (default is {@link NumericFormat#FLOAT})
	 */
	public FormatBuilder type(NumericFormat numeric) {
		this.numeric = notNull(numeric);
		return this;
	}

	/**
	 * Builds this format.
	 * @return Vulkan format
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public VkFormat build() {
		// Build component format
		final StringBuilder layout = new StringBuilder();
		final int size = bytes * Byte.SIZE;
		for(int n = 0; n < count; ++n) {
			layout.append(components.charAt(n));
			layout.append(size);
		}

		// Build format string
		final char ch = signed ? 'S' : 'U';
		final String format = String.format("%s_%c%s", layout, ch, numeric.name());

		// Lookup format
		return VkFormat.valueOf(format);
	}
}

