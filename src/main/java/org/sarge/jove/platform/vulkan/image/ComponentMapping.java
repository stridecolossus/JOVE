package org.sarge.jove.platform.vulkan.image;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;

/**
 * The <i>component mapping</i> is a utility class used to construct the channel swizzles for a Vulkan image view.
 * @author Sarge
 */
public record ComponentMapping(List<VkComponentSwizzle> swizzles) {
	private static final int SIZE = 4;

	/**
	 * Identity component mapping.
	 * @see VkComponentSwizzle#IDENTITY
	 */
	public static final ComponentMapping IDENTITY = new ComponentMapping(Collections.nCopies(SIZE, VkComponentSwizzle.IDENTITY));

	/**
	 * Constructor.
	 * @param swizzles Component mapping swizzles in RGBA order
	 * @throws IllegalArgumentException if {@link #swizzles} does not contain 4 swizzles
	 */
	public ComponentMapping {
		if(swizzles.size() != SIZE) {
			throw new IllegalArgumentException();
		}
		swizzles = List.copyOf(swizzles);
	}

	/**
	 * Creates the Vulkan descriptor for this component mapping.
	 * @return Component mapping descriptor
	 */
	public VkComponentMapping build() {
		final var mapping = new VkComponentMapping();
		final var array = swizzles.toArray(VkComponentSwizzle[]::new);
		mapping.r = array[0];
		mapping.g = array[1];
		mapping.b = array[2];
		mapping.a = array[3];
		return mapping;
	}

	/**
	 * Creates a component mapping from the given string specifying the swizzle for each channel of an image.
	 * <p>
	 * Each character in the string is one of the following:
	 * <ul>
	 * <li>an RGBA channel, e.g. {@code R} for {@link VkComponentSwizzle#R}</li>
	 * <li>{@code =} for {@link VkComponentSwizzle#IDENTITY}</li>
	 * <li>{@code 1} for {@link VkComponentSwizzle#ONE}</li>
	 * <li>{@code 0} for {@link VkComponentSwizzle#ZERO}</li>
	 * </ul>
	 * @param components Component mapping string specification (case sensitive) in RGBA order
	 * @return Component mapping
	 * @throws IllegalArgumentException if {@link #components} is empty or longer than 4 characters in length
	 * @throws IllegalArgumentException for an unsupported swizzle character
	 * @see #map(char)
	 */
	public static ComponentMapping of(String components) {
		// Validate
		final int length = components.length();
		if(length == 0) {
			throw new IllegalArgumentException("Component mapping cannot be empty");
		}
		if(length > SIZE) {
			throw new IllegalArgumentException("Invalid component mapping length: " + components);
		}

		// Map components
		final var swizzles = new VkComponentSwizzle[SIZE];
		for(int n = 0; n < length; ++n) {
			swizzles[n] = map(components.charAt(n));
		}

		// Unspecified channels are the identity swizzle
		for(int n = length; n < SIZE; ++n) {
			swizzles[n] = VkComponentSwizzle.IDENTITY;
		}

		// Convert to mapping
		return new ComponentMapping(List.of(swizzles));
	}

	/**
	 * Maps a component mapping character to the corresponding swizzle.
	 * @param swizzle Swizzle character
	 * @return Component swizzle
	 * @throws IllegalArgumentException if the swizzle is not supported
	 */
	public static VkComponentSwizzle map(char swizzle) {
		return switch(swizzle) {
			case 'R' -> VkComponentSwizzle.R;
			case 'G' -> VkComponentSwizzle.G;
			case 'B' -> VkComponentSwizzle.B;
			case 'A' -> VkComponentSwizzle.A;
			case '=' -> VkComponentSwizzle.IDENTITY;
			case '1' -> VkComponentSwizzle.ONE;
			case '0' -> VkComponentSwizzle.ZERO;
			default -> throw new IllegalArgumentException("Unsupported swizzle mapping: " + swizzle);
		};
	}
}
