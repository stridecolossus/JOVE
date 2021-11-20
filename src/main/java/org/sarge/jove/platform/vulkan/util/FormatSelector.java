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
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>format selector</i> is used to select an appropriate format from a list of candidates.
 * <p>
 * The {@link VkFormatProperties} for a given format can be retrieved via the {@link PhysicalDevice#properties()} method.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * 	// Create a selector for a depth-stencil with optimal tiling
 * 	PhysicalDevice dev = ...
 * 	Predicate predicate = FormatSelector.predicate(Set.of(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT), true);
 * 	FormatSelector selector = new FormatSelector(dev::properties, predicate);
 *
 * 	// The selector is also a predicate
 * 	selector.test(VkFormat.D32_SFLOAT);
 *
 * 	// Select a depth-stencil format
 * 	VkFormat = selector.select(List.of(VkFormat.D32_SFLOAT, ...));
 * </pre>
 * <p>
 * @author Sarge
 */
public class FormatSelector implements Predicate<VkFormat> {
	private final Function<VkFormat, VkFormatProperties> mapper;
	private final Predicate<VkFormatProperties> predicate;

	/**
	 * Constructor.
	 * @param mapper		Function to lookup the properties for a given format
	 * @param predicate		Format predicate
	 */
	public FormatSelector(Function<VkFormat, VkFormatProperties> mapper, Predicate<VkFormatProperties> predicate) {
		this.mapper = notNull(mapper);
		this.predicate = notNull(predicate);
	}

	@Override
	public boolean test(VkFormat format) {
		final VkFormatProperties props = mapper.apply(format);
		return predicate.test(props);
	}

	/**
	 * Selects the best format from the given candidates.
	 * @param formats Candidate formats
	 * @return Selected format
	 */
	public Optional<VkFormat> select(List<VkFormat> formats) {
		return formats
				.stream()
				.filter(this::test)
				.findAny();
	}

	/**
	 * Creates a format predicate for the given features.
	 * @param features		Format features
	 * @param optimal		Whether to select optimal or linear features
	 * @return Format predicate
	 */
	public static Predicate<VkFormatProperties> predicate(Set<VkFormatFeature> features, boolean optimal) {
		final int mask = IntegerEnumeration.mask(features);
		return props -> {
			final int actual = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
			return MathsUtil.isMask(mask, actual);
		};
	}
}
