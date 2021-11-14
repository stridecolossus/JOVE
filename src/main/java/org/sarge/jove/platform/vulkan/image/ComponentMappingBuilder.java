package org.sarge.jove.platform.vulkan.image;

import java.util.Arrays;

import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;

/**
 * The <i>component mapping builder</i> is a helper used to construct the component mapping for an image view.
 * @see DescriptorLayout
 * @author Sarge
 */
public final class ComponentMappingBuilder {
	/**
	 * Number of components.
	 */
	private static final int SIZE = 4;

	/**
	 * Identity component mapping.
	 */
	public static final VkComponentMapping IDENTITY;

	static {
		final VkComponentSwizzle[] swizzle = new VkComponentSwizzle[SIZE];
		Arrays.fill(swizzle, VkComponentSwizzle.IDENTITY);
		IDENTITY = build(swizzle);
	}

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
	 * @throws IllegalArgumentException if the mapping is empty or is not 4 characters in length
	 * @throws IllegalArgumentException if a channel swizzle is not supported
	 */
	public static VkComponentMapping build(String mapping) {
		// Validate
		if(mapping.length() != SIZE) throw new IllegalArgumentException(String.format("Invalid component mapping [%s]", mapping));

		// Build swizzle array
		final VkComponentSwizzle[] swizzle = new VkComponentSwizzle[SIZE];
		Arrays.setAll(swizzle, n -> swizzle(mapping.charAt(n)));

		// Build component mapping
		return build(swizzle);
	}

	/**
	 * @param swizzle Component mapping array
	 * @return New component mapping
	 */
	private static VkComponentMapping build(VkComponentSwizzle[] swizzle) {
		final VkComponentMapping components = new VkComponentMapping();
		components.r = swizzle[0];
		components.g = swizzle[1];
		components.b = swizzle[2];
		components.a = swizzle[3];
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
