package org.sarge.jove.platform.vulkan.image;

import java.util.Arrays;

import org.sarge.jove.platform.vulkan.VkComponentMapping;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;

/**
 * The <i>component mapping</i> is a helper class used to construct the RGBA swizzle for an image view.
 * @see VkComponentMapping
 * @see VkComponentSwizzle
 * @author Sarge
 */
public final class ComponentMapping {
	/**
	 * Identity component mapping.
	 */
	public static final ComponentMapping IDENTITY = new ComponentMapping();

	static {
		Arrays.fill(IDENTITY.swizzle, VkComponentSwizzle.IDENTITY);
	}

	/**
	 * Creates a component mapping from the given {@link #mapping} string which specifies the component swizzle for each channel of an image.
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
	 * @throws IllegalArgumentException if the mapping is empty or is longer than 4 characters in length
	 * @throws IllegalArgumentException for an unsupported channel swizzle character
	 */
	public static ComponentMapping of(String mapping) {
		final ComponentMapping instance = new ComponentMapping();
		instance.populate(mapping);
		return instance;
	}

	private final VkComponentSwizzle[] swizzle = new VkComponentSwizzle[4];

	private ComponentMapping() {
	}

	/**
	 * Populates the component mapping.
	 */
	private void populate(String mapping) {
		// Validate
		final int len = mapping.length();
		if(len == 0) throw new IllegalArgumentException("Component mapping cannot be empty");
		if(len > swizzle.length) throw new IllegalArgumentException(String.format("Invalid component mapping length [%s]", mapping));

		// Init swizzle array
		for(int n = 0; n < len; ++n) {
			swizzle[n] = swizzle(mapping.charAt(n));
		}
	}

	/**
	 * Creates the descriptor for this component mapping.
	 * @return Component mapping descriptor
	 */
	public VkComponentMapping build() {
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
