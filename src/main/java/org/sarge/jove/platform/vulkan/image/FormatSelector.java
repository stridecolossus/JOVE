package org.sarge.jove.platform.vulkan.image;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

/**
 * A <i>format selector</i> is used to select an appropriate format from a list of candidates.
 * <p>
 * The {@link VkFormatProperties} for a given format can be retrieved via the {@link PhysicalDevice#properties()} method.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create a selector for a depth-stencil with optimal tiling
 * PhysicalDevice dev = ...
 * Predicate predicate = FormatSelector.predicate(Set.of(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT), true);
 * FormatSelector selector = new FormatSelector(dev::properties, predicate);
 *
 * // Select a depth-stencil format
 * VkFormat format = selector.select(List.of(VkFormat.D32_SFLOAT)).orElseThrow();
 * </pre>
 * <p>
 * @author Sarge
 */
public class FormatSelector {
	/**
	 * Helper - Creates a format filter that matches the given set of features.
	 * @param features		Required format features
	 * @param optimal		Whether to match on the <i>optimal</i> or <i>linear</i> tiling features
	 * @return Format filter
	 */
	public static Predicate<VkFormatProperties> filter(Set<VkFormatFeature> features, boolean optimal) {
		Check.notEmpty(features);
		final int bits = IntegerEnumeration.reduce(features);
		return props -> {
			final Mask mask = new Mask(optimal ? props.optimalTilingFeatures : props.linearTilingFeatures);
			return mask.contains(bits);
		};
	}

	private final PhysicalDevice dev;
	private final Predicate<VkFormatProperties> filter;

	/**
	 * Constructor.
	 * @param dev			Physical device
	 * @param filter		Format selector
	 */
	public FormatSelector(PhysicalDevice dev, Predicate<VkFormatProperties> filter) {
		this.dev = notNull(dev);
		this.filter = notNull(filter);
	}

	/**
	 * Selects a format from the given list of candidates.
	 * @param candidates Candidate formats
	 * @return Selected format
	 */
	public Optional<VkFormat> select(VkFormat... candidates) {
		return select(Arrays.asList(candidates));
	}

	/**
	 * Selects a format from the given list of candidates.
	 * @param candidates Candidate formats
	 * @return Selected format
	 */
	public Optional<VkFormat> select(List<VkFormat> candidates) {
		return candidates.stream().filter(this::matches).findAny();
	}

	/**
	 * Matches a candidate format.
	 */
	private boolean matches(VkFormat format) {
		final VkFormatProperties props = dev.properties(format);
		return filter.test(props);
	}
}
