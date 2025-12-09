package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>format selector</i> is a utility used to choose an appropriate format from a list of candidates.
 * @author Sarge
 */
public class FormatSelector {
	private final Function<VkFormat, VkFormatProperties> provider;
	private final Map<VkFormat, VkFormatProperties> cache = new HashMap<>();

	/**
	 * Constructor.
	 * @param provider Format properties provider
	 */
	public FormatSelector(Function<VkFormat, VkFormatProperties> provider) {
		this.provider = requireNonNull(provider);
	}

	/**
	 * Selects a matching format from the given candidates.
	 * @param formats		Candidate formats
	 * @param matcher		Format matcher
	 * @return Selected format
	 * @see #filter(boolean, Set)
	 */
	public Optional<VkFormat> select(List<VkFormat> formats, Predicate<VkFormatProperties> matcher) {
		return formats
				.stream()
				.filter(format -> matcher.test(properties(format)))
				.findAny();
	}

	private VkFormatProperties properties(VkFormat format) {
		return cache.computeIfAbsent(format, provider);
	}

	/**
	 * Creates a filter for formats matching the given set of features.
	 * @param optimal		Whether to select optimal properties
	 * @param features		Required features
	 * @return Format matcher
	 */
	public static Predicate<VkFormatProperties> filter(boolean optimal, Set<VkFormatFeatureFlags> features) {
		final EnumMask<VkFormatFeatureFlags> mask = new EnumMask<>(features);
		return properties -> {
			final EnumMask<VkFormatFeatureFlags> supported = optimal ? properties.optimalTilingFeatures : properties.linearTilingFeatures;
			if(supported == null) {
				return false;
			}
			return supported.contains(mask.bits());
		};
	}
}
