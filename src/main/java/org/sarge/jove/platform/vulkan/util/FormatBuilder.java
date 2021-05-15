package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Component;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Builder for a Vulkan format.
 * <p>
 * Finding a Vulkan format within the {@link VkFormat} enumeration can be difficult given the large number of available formats.
 * However the naming convention is very consistent and therefore a format can often be more easily specified programatically.
 * <p>
 * Example:
 * <pre>
 * VkFormat format = new FormatBuilder()
 *      .components("BGRA")			// BGRA
 *      .bytes(2)					// 16
 *      .signed(false)				// U
 *      .type(Type.NORMALIZED)		// NORM
 *      .build();
 * // Returns <code>VK_FORMAT_B16G16R16A16_UNORM</code>
 * </pre>
 * @see VkFormat
 */
public class FormatBuilder {
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
				case "int", "integer" -> Type.INTEGER;
				case "float" -> Type.FLOAT;
				default -> throw new IllegalArgumentException("Unsupported component type: " + type);
			};
		}

		private final String token;

		private Type(String token) {
			this.token = token;
		}
	}

	/**
	 * Helper - Determines the format of the given image.
	 * @param image Image
	 * @return Image format
	 */
	public static VkFormat format(ImageData image) {
		// TODO - should probably be deriving this from the image header somehow?
		return new FormatBuilder()
				.components(image.components().size())
				.bytes(1)
				.signed(true)
				.type(Type.RGB)			// TODO - assumes SRGB colour-space
				.build();
	}

	/**
	 * Helper - Determines the format for the given component layout.
	 * @param layout component layout
	 * @return Component format
	 */
	public static VkFormat format(Component.Layout layout) {
		return new FormatBuilder()
				.components(layout.size())
				.bytes(layout.bytes())
				.type(Type.of(layout.type()))
				.build();
	}

	private String components = RGBA;
	private int num = 4;
	private int bytes = 4;
	private Type type = Type.FLOAT;
	private boolean signed = true;

	/**
	 * Sets the colour component characters, e.g. {@code ARGB}.
	 * @param components Colour component string
	 * @throws IllegalArgumentException if the given components string is empty, contains an invalid character, or is longer than 4 components
	 */
	public FormatBuilder components(String components) {
		Check.range(components.length(), 1, 4);
		if(components.chars().anyMatch(ch -> RGBA.indexOf(ch) == -1)) throw new IllegalArgumentException("Invalid components specifier: " + components);
		this.components = components;
		return this;
	}

	/**
	 * Sets the number of components.
	 * @param num Number of components 1..4
	 */
	public FormatBuilder components(int num) {
		this.num = Check.range(num, 1, 4);
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
	 * Sets the data type.
	 * @param type Data type (default is {@link Type#FLOAT})
	 * @see Type#of(Class)
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
		// Validate format
		if(num > components.length()) throw new IllegalArgumentException(String.format("Invalid components specification: components=%s length=%d", components, num));

		// Build component layout
		final StringBuilder layout = new StringBuilder();
		final int size = bytes * Byte.SIZE;
		for(int n = 0; n < num; ++n) {
			layout.append(components.charAt(n));
			layout.append(size);
		}

		// Build format string
		final char ch = signed ? 'S' : 'U';
		final String format = String.format("%s_%c%s", layout, ch, type.token);

		// Lookup format
		return VkFormat.valueOf(format);
	}
}

