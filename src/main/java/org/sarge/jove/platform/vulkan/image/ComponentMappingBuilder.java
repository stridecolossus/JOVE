package org.sarge.jove.platform.vulkan.image;

import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;

/**
 * The <i>mapping builder</i> is a helper used to construct the component mapping for an image view.
 * @author Sarge
 */
public final class ComponentMappingBuilder {
	/**
	 * Identity component mapping.
	 */
	public static final VkComponentMapping IDENTITY = build("====");

	private ComponentMappingBuilder() {
	}

	/**
	 * Builds a component mapping from the given specification.
	 * <p>
	 * The mapping specification string specifies the component swizzle for each channel on an RGBA image.
	 * <p>
	 * The character corresponding to each channel is one of the following:
	 * <ul>
	 * <li>an RGBA character, e.g. {@link VkComponentSwizzle#R}</li>
	 * <li>{@code =} for {@link VkComponentSwizzle#IDENTITY}</li>
	 * <li>{@code 1} for {@link VkComponentSwizzle#ONE}</li>
	 * <li>{@code 0} for {@link VkComponentSwizzle#ZERO}</li>
	 * </ul>
	 * @param mapping Mapping specification
	 * @return Component mapping
	 * @throws IllegalArgumentException if the mapping is empty or invalid
	 * @throws IllegalArgumentException if a channel swizzle is not supported
	 */
	public static VkComponentMapping build(String mapping) {
		if(mapping.length() != 4) throw new IllegalArgumentException(String.format("Invalid component mapping [%s]", mapping));
		final VkComponentMapping components = new VkComponentMapping();
		components.r = swizzle(mapping.charAt(0));
		components.g = swizzle(mapping.charAt(1));
		components.b = swizzle(mapping.charAt(2));
		components.a = swizzle(mapping.charAt(3));
		return components;
	}

	/**
	 * Maps a component mapping character to the corresponding swizzle.
	 * @param mapping Swizzle mapping
	 * @return Component swizzle
	 * @throws IllegalArgumentException if the mapping is not supported
	 */
	private static VkComponentSwizzle swizzle(char mapping) {
		return switch(mapping) {
			case 'R' -> VkComponentSwizzle.R;
			case 'G' -> VkComponentSwizzle.G;
			case 'B' -> VkComponentSwizzle.B;
			case 'A' -> VkComponentSwizzle.A;
			case '=' -> VkComponentSwizzle.IDENTITY;
			case '1' -> VkComponentSwizzle.ONE;
			case '0' -> VkComponentSwizzle.ZERO;
			default -> throw new IllegalArgumentException("Unsupported swizzle mapping: " + mapping);
		};
	}
}
