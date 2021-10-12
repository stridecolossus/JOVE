package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * The <i>format builder</i> is used to programatically select a Vulkan format.
 * <p>
 * Finding a format in the {@link VkFormat} enumeration can be difficult given the number of values.
 * However the naming convention is highly consistent and it is therefore often easier to specify the format programatically.
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
 * The {@link #format(Layout)} convenience method can also be used to determine the format from a vertex {@link Layout}.
 * <p>
 * @see VkFormat
 * @author Sarge
 */
public class FormatBuilder {
	/**
	 * Vulkan components template.
	 */
	public static final String RGBA = "RGBA";

	/**
	 * Component data-type.
	 */
	public enum Type {
		INT,
		FLOAT,
		NORM,
		SCALED,
		RGB;

		/**
		 * Maps the given Java type to a Vulkan component type.
		 * @param type Type
		 * @return Vulkan type
		 * @throws IllegalArgumentException if the type is not supported
		 */
		public static Type of(Class<?> type) {
			return switch(type.getSimpleName().toLowerCase()) {
				case "float" -> FLOAT;
				case "integer", "int", "short" -> INT;
				case "byte" -> NORM;
				default -> throw new IllegalArgumentException("Unsupported data type: " + type);
			};
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
				.type(layout.type())
				.signed(layout.signed())
				.build();
	}

	private String components = RGBA;
	private int count = 4;
	private int bytes = 4;
	private Type type = Type.FLOAT;
	private boolean signed = true;

	/**
	 * Sets the colour component template characters, e.g. {@code ARGB}.
	 * @param template Colour component template string
	 * @throws IllegalArgumentException if the given template is empty, contains an invalid character, or is longer than 4 components
	 */
	public FormatBuilder components(String components) {
		Check.range(components.length(), 1, 4);
		if(components.chars().anyMatch(ch -> RGBA.indexOf(ch) == -1)) throw new IllegalArgumentException("Invalid components template: " + components);
		this.components = components;
		this.count = Math.min(count, components.length());
		return this;
	}

	/**
	 * Sets the number of components.
	 * @param count Number of components
	 * @throws IllegalArgumentException if the given count exceeds the components template
	 * @see #components(String)
	 */
	public FormatBuilder count(int count) {
		this.count = Check.range(count, 1, 4);
		return this;
	}

	/**
	 * Sets the number of bytes per component: 1, 2, 4 or 8.
	 * @param bytes Number of bytes per component (default is <code>4</code>)
	 * @throws IllegalArgumentException if the number of bytes is invalid
	 */
	public FormatBuilder bytes(int bytes) {
		if((bytes < 1) || (bytes > 8) || !MathsUtil.isPowerOfTwo(bytes)) {
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
	 * Sets the data type.
	 * @param type Data type
	 * @see Type#of(Class)
	 */
	public FormatBuilder type(Class<?> type) {
		this.type = Type.of(type);
		return this;
	}

	/**
	 * Builds the format identifier.
	 * @return Vulkan format
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public VkFormat build() {
		// Validate format
		if(count > components.length()) {
			throw new IllegalArgumentException(String.format("Invalid components specification: components=%s length=%d", components, count));
		}

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

