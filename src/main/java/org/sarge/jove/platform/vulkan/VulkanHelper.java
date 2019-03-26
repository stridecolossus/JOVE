package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.range;

import java.util.Set;
import java.util.StringJoiner;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * General Vulkan utilities.
 * @author Sarge
 */
public final class VulkanHelper {
	private VulkanHelper() {
	}

	/**
	 * Builds for a Vulkan format.
	 * @see VkFormat
	 */
	public static class FormatBuilder {
		/**
		 * Format data type.
		 */
		public enum Type {
			INT,
			FLOAT,
			NORMALIZED,
			SCALED,
			SRGB,
		}

		private static final Set<Integer> SIZES = Set.of(8, 16, 32, 64);
		private static final String RGBA = "RGBA";
		private static final String ARGB = "ARGB"; // TODO - swap builder option

		private int components = 3;
		private int size = 32;
		private Type type = Type.FLOAT;
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
		 * Sets the component size: 8, 16, 32 or 64.
		 * @param size Component size (default is <tt>32</tt>)
		 * @throws IllegalArgumentException if the size is invalid
		 */
		public FormatBuilder size(int size) {
			if(!SIZES.contains(size)) throw new IllegalArgumentException("Unsupported component size: " + size);
			this.size = size;
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
		 * @param type Data type (default is {@link Type#FLOAT})
		 */
		public FormatBuilder type(Type type) {
			this.type = notNull(type);
			return this;
		}

		/**
		 * Builds the format identifier.
		 * @return Vulkan format
		 * @throws IllegalArgumentException TODO
		 */
		public VkFormat build() {
			// Build component layout
			final StringBuilder layout = new StringBuilder();
			for(int n = 0; n < components; ++n) {
				layout.append(RGBA.charAt(n));
				layout.append(size);
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
