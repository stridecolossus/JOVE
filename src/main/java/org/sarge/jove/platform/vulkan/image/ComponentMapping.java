package org.sarge.jove.platform.vulkan.image;

import java.util.Arrays;

import org.sarge.jove.platform.vulkan.*;

/**
 * The <i>component mapping</i> is a utility class used to construct the channel swizzles for a Vulkan image view.
 * @author Sarge
 */
public final class ComponentMapping {
	private static final int SIZE = 4;

	private ComponentMapping() {
	}

	/**
	 * @return Identity component mapping
	 * @see VkComponentSwizzle#IDENTITY
	 */
	public static VkComponentMapping identity() {
		final var swizzle = new VkComponentSwizzle[SIZE];
		Arrays.fill(swizzle, VkComponentSwizzle.IDENTITY);
		return build(swizzle);
	}

	/**
	 * Creates a component mapping from the given {@link #mapping} string which specifies the component swizzle for each channel of an image.
	 * <p>
	 * The character corresponding to each channel is one of the following:
	 * <ul>
	 * <li>an RGBA character, e.g. {@code R} for {@link VkComponentSwizzle#R}</li>
	 * <li>{@code =} for {@link VkComponentSwizzle#IDENTITY}</li>
	 * <li>{@code 1} for {@link VkComponentSwizzle#ONE}</li>
	 * <li>{@code 0} for {@link VkComponentSwizzle#ZERO}</li>
	 * </ul>
	 * @param mapping Mapping specification
	 * @return Component mapping
	 * @throws IllegalArgumentException if {@link #mapping} is empty or longer than 4 characters in length
	 * @throws IllegalArgumentException for an unsupported channel swizzle character
	 */
	public static VkComponentMapping of(String mapping) {
		// Validate
		final int len = mapping.length();
		if(len == 0) throw new IllegalArgumentException("Component mapping cannot be empty");
		if(len > SIZE) throw new IllegalArgumentException("Invalid component mapping length:" + mapping);

		// Map component swizzles
		final var swizzle = new VkComponentSwizzle[SIZE];
		for(int n = 0; n < len; ++n) {
			swizzle[n] = swizzle(mapping.charAt(n));
		}
		for(int n = len; n < SIZE; ++n) {
			swizzle[n] = VkComponentSwizzle.ONE;
		}

		// Create mapping
		return build(swizzle);
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

	/**
	 * @param swizzle Component swizzles
	 * @return New component mapping
	 */
	private static VkComponentMapping build(VkComponentSwizzle[] swizzle) {
		final var mapping = new VkComponentMapping();
		mapping.r = swizzle[0];
		mapping.g = swizzle[1];
		mapping.b = swizzle[2];
		mapping.a = swizzle[3];
		return mapping;
	}
}
