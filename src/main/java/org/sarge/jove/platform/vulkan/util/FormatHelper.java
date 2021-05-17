package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Layout;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * The <i>format helper</i> is used to programatically select a Vulkan format.
 * <p>
 * Finding a format in the {@link VkFormat} enumeration can be difficult given the number of values.
 * However the naming convention is highly consistent and it is often easier to specify the format in code.
 * <p>
 * The {@link #format(Layout)} convenience method can also be used to determine the format from a component {@link Layout}.
 * <p>
 * Examples:
 * <pre>
 * // Construct a format: <code>B16G16R16A16_UNORM</code>
 * VkFormat format = new FormatHelper()
 *      .template("BGRA")			// BGRA
 *      .bytes(2)					// 16
 *      .signed(false)				// U
 *      .type(Type.NORMALIZED)		// NORM
 *      .build();
 *
 * // Determine format from a component layout: <code>R32G32B32_SFLOAT</code>
 * VkFormat point = FormatHelper.format(Point.LAYOUT);
 * </pre>
 * @see VkFormat
 */
public class FormatHelper {
	/**
	 * Default component layout.
	 */
	public static final String RGBA = "RGBA";

	/**
	 * Reverse component layout.
	 */
	public static final String ARGB = "ARGB";

	/**
	 * Surface format components.
	 */
	public static final String BGRA = "BGRA";

	/**
	 * Component data-type.
	 */
	public enum Type {
		INTEGER("INT"),
		FLOAT("FLOAT"),
		NORMALIZED("NORM"),
		SCALED("SCALED"),
		RGB("RGB");

		/**
		 * Maps the given component type to the corresponding format type suffix.
		 * @param type Component type
		 * @return Format type
		 * @throws IllegalArgumentException if the type is unsupported
		 */
		public static Type of(Class<?> type) {
			return switch(type.getSimpleName().toLowerCase()) {
				case "float"				-> Type.FLOAT;
				case "int", "integer"		-> Type.INTEGER;
				case "byte"					-> Type.RGB;
				default -> throw new IllegalArgumentException("Unsupported component type: " + type);
			};
		}

		private final String token;

		private Type(String token) {
			this.token = token;
		}
	}

	/**
	 * Helper - Determines the format for the given component layout.
	 * @param layout component layout
	 * @return Component format
	 */
	public static VkFormat format(Layout layout) {
		return new FormatHelper()
				.count(layout.size())
				.bytes(layout.bytes())
				.type(Type.of(layout.type()))
				.build();
	}

	private String template = RGBA;
	private int count = 4;
	private int bytes = 4;
	private Type type = Type.FLOAT;
	private boolean signed = true;

	/**
	 * Sets the colour component template characters, e.g. {@code ARGB}.
	 * @param template Colour component template string
	 * @throws IllegalArgumentException if the given template is empty, contains an invalid character, or is longer than 4 components
	 */
	public FormatHelper template(String template) {
		Check.range(template.length(), 1, 4);
		if(template.chars().anyMatch(ch -> RGBA.indexOf(ch) == -1)) throw new IllegalArgumentException("Invalid components specifier: " + template);
		this.template = template;
		return this;
	}

	/**
	 * Sets the number of components.
	 * @param count Number of components 1..4
	 */
	public FormatHelper count(int count) {
		this.count = Check.range(count, 1, 4);
		return this;
	}

	/**
	 * Sets the number of bytes per component: 1, 2, 4 or 8.
	 * @param bytes Number of bytes per component (default is <code>4</code>)
	 * @throws IllegalArgumentException if the number of bytes is invalid
	 */
	public FormatHelper bytes(int bytes) {
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
	public FormatHelper signed(boolean signed) {
		this.signed = signed;
		return this;
	}

	/**
	 * Sets the data type.
	 * @param type Data type (default is {@link Type#FLOAT})
	 * @see Type#of(Class)
	 */
	public FormatHelper type(Type type) {
		this.type = notNull(type);
		return this;
	}

	/**
	 * Builds the format identifier.
	 * @return Vulkan format
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public VkFormat build() {
		// Validate format
		if(count > template.length()) {
			throw new IllegalArgumentException(String.format("Invalid components specification: components=%s length=%d", template, count));
		}

		// Build component layout
		final StringBuilder layout = new StringBuilder();
		final int size = bytes * Byte.SIZE;
		for(int n = 0; n < count; ++n) {
			layout.append(template.charAt(n));
			layout.append(size);
		}

		// Build format string
		final char ch = signed ? 'S' : 'U';
		final String format = String.format("%s_%c%s", layout, ch, type.token);

		// Lookup format
		return VkFormat.valueOf(format);
	}
}

