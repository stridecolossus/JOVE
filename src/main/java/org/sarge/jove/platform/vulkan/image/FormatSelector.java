package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.util.BitMask;
import static org.sarge.lib.Validation.*;

/**
 * A <i>format selector</i> is a helper utility used to choose an appropriate format from a list of candidates.
 * <p>
 * The {@link VkFormatProperties} for a given format are retrieved via the {@link PhysicalDevice#properties(VkFormat)} method.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * PhysicalDevice dev = ...
 * Predicate predicate = FormatSelector.filter(true, VkFormatFeature.DEPTH_STENCIL_ATTACHMENT);
 * FormatSelector selector = new FormatSelector(dev, predicate);
 * VkFormat format = selector.select(VkFormat.D32_SFLOAT, ...).orElseThrow();
 * </pre>
 * <p>
 * @author Sarge
 */
public class FormatSelector {
	/**
	 * Helper - Creates a format filter that matches the given set of features.
	 * @param optimal		Whether to match on the <i>optimal</i> or <i>linear</i> tiling features
	 * @param required		Required format features
	 * @return Format filter
	 */
	public static Predicate<VkFormatProperties> filter(boolean optimal, Set<VkFormatFeature> required) {
		requireNotEmpty(required);
		final BitMask<VkFormatFeature> mask = new BitMask<>(required);
		return props -> {
			final BitMask<VkFormatFeature> supported = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
			return supported.contains(mask);
		};
	}

	// TODO - should be a function?
	private final PhysicalDevice dev;
	private final Predicate<VkFormatProperties> filter;

	/**
	 * Constructor.
	 * @param dev			Physical device
	 * @param filter		Format selector
	 */
	public FormatSelector(PhysicalDevice dev, Predicate<VkFormatProperties> filter) {
		this.dev = requireNonNull(dev);
		this.filter = requireNonNull(filter);
	}

	/**
	 * Convenience constructor to select matching format features.
	 * @param dev			Physical device
	 * @param optimal		Whether to match on the <i>optimal</i> or <i>linear</i> tiling features
	 * @param features		Required format features
	 * @see #filter(boolean, Set)
	 */
	public FormatSelector(PhysicalDevice dev, boolean optimal, VkFormatFeature... features) {
		this(dev, filter(optimal, Set.of(features)));
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
