package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.range;

import java.util.StringJoiner;

import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.util.MathsUtil;

/**
 * General Vulkan utilities.
 * @author Sarge
 */
public final class VulkanHelper {
	private VulkanHelper() {
	}

	/**
	 * Maps a JOVE component descriptor to the equivalent Vulkan format.
	 * @param c Component
	 * @return Format
	 * @throws IllegalArgumentException if the format is not supported
	 */
	public static VkFormat format(Vertex.Component c) {
		return new FormatBuilder()
			.type(c.type())
			.components(c.size())
			.bytes(c.bytes())
			.build();
	}

	/**
	 * Builder for a Vulkan format.
	 * <p>
	 * Finding a format within the enumeration can be difficult given the number available and the naming strategy.
	 * This intention of this builder is to simply specifying the common data/image formats.
	 * <p>
	 * Example:
	 * <pre>
	 * VkFormat format = new FormatBuilder()
	 *  .type(Type.NORMALIZED)		// NORM
	 *  .signed(false)				// U
	 *  .components(3)				// RGB
	 *  .bytes(2)					// 16
	 *  .build();
	 * // Returns <tt>VK_FORMAT_R16G16B16_UNORM</tt>
	 * </pre>
	 * @see VkFormat
	 */
	public static class FormatBuilder {
		private static final String RGBA = "RGBA";
		private static final String ARGB = "ARGB"; // TODO - swap builder option

		private int components = 3;
		private int bytes = 4;
		private Vertex.Component.Type type = Vertex.Component.Type.FLOAT;
		private boolean signed = true;

		/**
		 * Sets the number of components, e.g. 3 for RGB.
		 * @param components Number of components 1..4 (default is 3)
		 * @throws IllegalArgumentException if the number of components is not 1..4
		 */
		public FormatBuilder components(int components) {
			this.components = range(components, 1, 4);
			return this;
		}

		/**
		 * Sets the number of bytes per component: 1, 2, 4 or 8.
		 * @param bytes Number of bytes per component (default is <tt>4</tt>)
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
		 * @param signed Whether signed type (default is <tt>true</tt>)
		 */
		public FormatBuilder signed(boolean signed) {
			this.signed = signed;
			return this;
		}

		/**
		 * Sets the data type.
		 * @param type Data type (default is {@link Vertex.Component.Type#FLOAT})
		 */
		public FormatBuilder type(Vertex.Component.Type type) {
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
			for(int n = 0; n < components; ++n) {
				layout.append(RGBA.charAt(n));
				layout.append(bytes * Byte.SIZE);
			}

			// Build format string
			final String format = new StringJoiner("_")
				.add("VK_FORMAT")
				.add(layout.toString())
				.add((signed ? "S" : "U") + type.name().toUpperCase())
				.toString();

			// Lookup format
			return VkFormat.valueOf(format.toString());
		}
	}

	/**
	 * Default colour component flags containing all RGBA components.
	 * @see #mask(String)
	 */
	public static final int DEFAULT_COLOUR_COMPONENT = colourComponent("RGBA");

	/**
	 * Creates a colour component mask from the given string representation, e.g. <tt>RGBA</tt>.
	 * @param components Components string
	 * @return Colour component mask
	 * @throws IllegalArgumentException if a component is not a valid <tt>RGBA</tt> character
	 */
	public static int colourComponent(String components) {
		return components.chars().mapToObj(VulkanHelper::component).mapToInt(IntegerEnumeration::value).reduce(0, IntegerEnumeration.MASK);
	}

	/**
	 * Maps a colour component character.
	 */
	private static VkColorComponentFlag component(int ch) {
		switch(ch) {
		case 'R':	return VkColorComponentFlag.VK_COLOR_COMPONENT_R_BIT;
		case 'G':	return VkColorComponentFlag.VK_COLOR_COMPONENT_G_BIT;
		case 'B':	return VkColorComponentFlag.VK_COLOR_COMPONENT_B_BIT;
		case 'A':	return VkColorComponentFlag.VK_COLOR_COMPONENT_A_BIT;
		default:	throw new IllegalArgumentException("Invalid colour component: " + String.valueOf((char) ch));
		}
	}
}
