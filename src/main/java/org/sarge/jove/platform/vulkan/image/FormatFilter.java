package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>format filter</i> matches an image format against those supported by the hardware,
 * @see PhysicalDevice#properties(VkFormat)
 * @author Sarge
 */
public class FormatFilter implements Predicate<VkFormat >{
	private final Function<VkFormat, VkFormatProperties> provider;
	private final boolean optimal;
	private final EnumMask<VkFormatFeatureFlags> features;
	private final Map<VkFormat, VkFormatProperties> cache = new HashMap<>();

	/**
	 * Constructor.
	 * @param provider		Format properties provider
	 * @param boolean		Whether to select optimal or linear tiling features
	 * @param features		Required features
	 */
	public FormatFilter(Function<VkFormat, VkFormatProperties> provider, boolean optimal, Set<VkFormatFeatureFlags> features) {
		this.provider = requireNonNull(provider);
		this.optimal = optimal;
		this.features = new EnumMask<>(features);
	}
	// TODO - enum OPTIMAL, LINEAR, EITHER?

	@Override
	public boolean test(VkFormat format) {
		final VkFormatProperties properties = cache.computeIfAbsent(format, provider);
		final EnumMask<VkFormatFeatureFlags> supported = optimal ? properties.optimalTilingFeatures : properties.linearTilingFeatures;
		return supported.contains(features);
	}
}
