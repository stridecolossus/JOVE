package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.IntegerEnumeration;

/**
 * Vulkan enumeration wrapper.
 * This class has been code-generated.
 */
public enum VkColorComponentFlag implements IntegerEnumeration {
 	VK_COLOR_COMPONENT_R_BIT(1),
 	VK_COLOR_COMPONENT_G_BIT(2),
 	VK_COLOR_COMPONENT_B_BIT(4),
 	VK_COLOR_COMPONENT_A_BIT(8),
 	VK_COLOR_COMPONENT_FLAG_BITS_MAX_ENUM(2147483647);

	private final int value;

	private VkColorComponentFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}

	/**
	 * Default colour component flags containing all RGBA components.
	 * @see #mask(String)
	 */
	public static final int DEFAULT = mask("RGBA");

	/**
	 * Creates a colour component mask from the given string representation, e.g. <tt>RGBA</tt>.
	 * @param components Components string
	 * @return Colour component mask
	 * @throws IllegalArgumentException if a component is not a valid <tt>RGBA</tt> character
	 */
	public static int mask(String components) {
		return components.chars().mapToObj(VkColorComponentFlag::component).mapToInt(IntegerEnumeration::value).reduce(0, IntegerEnumeration.MASK);
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
