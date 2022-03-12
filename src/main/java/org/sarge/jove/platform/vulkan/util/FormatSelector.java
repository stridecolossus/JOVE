package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatFeature;
import org.sarge.jove.platform.vulkan.VkFormatProperties;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.Mask;
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
	 * Creates a selection test for the given format features.
	 * @param features		Required format features
	 * @param optimal		Whether to select <i>optimal</i> or <i>linear</i> features
	 * @return Format feature filter
	 */
	public static Predicate<VkFormatProperties> feature(Set<VkFormatFeature> features, boolean optimal) {
		final int mask = IntegerEnumeration.mask(features);
		return props -> {
			final int actual = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
			return new Mask(actual).contains(mask);
		};
	}

	private final Function<VkFormat, VkFormatProperties> mapper;
	private final Predicate<VkFormatProperties> predicate;

	/**
	 * Constructor.
	 * @param mapper 		Function to lookup the properties for a given format
	 * @param predicate		Selection criteria
	 */
	public FormatSelector(Function<VkFormat, VkFormatProperties> mapper, Predicate<VkFormatProperties> predicate) {
		this.mapper = notNull(mapper);
		this.predicate = notNull(predicate);
	}

	/**
	 * Selects the first matching format from the given list of candidates.
	 * @param candidates Candidate formats
	 * @return Selected format
	 */
	public Optional<VkFormat> select(List<VkFormat> candidates) {
		Check.notEmpty(candidates);
		return candidates
				.stream()
				.filter(this::matches)
				.findAny();
	}

	/**
	 * Retrieves the properties for the given format and applies the selection test.
	 */
	private boolean matches(VkFormat format) {
		final VkFormatProperties props = mapper.apply(format);
		return predicate.test(props);
	}
}
