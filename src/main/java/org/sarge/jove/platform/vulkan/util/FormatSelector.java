package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatFeature;
import org.sarge.jove.platform.vulkan.VkFormatProperties;
import org.sarge.jove.util.MathsUtil;

/**
 * The <i>format selector</i> is a helper used to selected an appropriate image format from a set of candidates.
 * <p>
 * A format is selected by querying the {@link VkFormatProperties} for a candidate format and matching against a {@link VkFormatFeature} mask.
 * The {@link VkFormatProperties} consist of <i>optimal</i> or <i>linear</i> features for a given format.
 * Note that the selector caches the query results for each format.
 * <p>
 * Example to select a depth-stencil format:
 * <pre>
 *  // Create selector
 *  PhysicalDevice dev = ...
 *  FormatSelector selector = new FormatSelector(dev::properties);
 *
 *  // Init candidate formats
 *  Set features = Set.of(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT);
 *  List candidates = List.of(VkFormat.D32_SFLOAT);
 *
 *  // Select optimal format
 *  optimal = selector.select(true, features, candidates);
 *
 *  // Or select linear format
 *  linear = selector.select(false, features, candidates);
 * </pre>
 * <p>
 * @author Sarge
 */
public class FormatSelector {
	private final Function<VkFormat, VkFormatProperties> func;
	private final Map<VkFormat, VkFormatProperties> cache = new HashMap<>();

	/**
	 * Constructor.
	 * @param func Function to retrieve the properties for a given format
	 */
	public FormatSelector(Function<VkFormat, VkFormatProperties> func) {
		this.func = notNull(func);
	}

	/**
	 * Selects an image format from the given candidates.
	 * @param optimal			Whether to select optimal or linear tiling features
	 * @param features			Required format feature(s)
	 * @param candidates		Candidate formats
	 * @return Selected format
	 */
	public Optional<VkFormat> select(boolean optimal, Set<VkFormatFeature> features, List<VkFormat> candidates) {
		final int mask = IntegerEnumeration.mask(features);
		return candidates
				.stream()
				.filter(f -> matches(f, optimal, mask))
				.findAny();
	}

	/**
	 * Matches a format.
	 * @param format			Candidate format
	 * @param optimal			Whether to select optimal or linear tiling features
	 * @param features			Required format feature(s)
	 * @return
	 */
	private boolean matches(VkFormat format, boolean optimal, int features) {
		final VkFormatProperties props = cache.computeIfAbsent(format, ignored -> func.apply(format));
		final int mask = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
		return MathsUtil.isMask(mask, features);
	}
}
