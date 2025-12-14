package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

class FormatFilterTest {
	private Function<VkFormat, VkFormatProperties> provider;
	private VkFormatProperties properties;

	@BeforeEach
	void before() {
		properties = new VkFormatProperties();
		provider = _ -> properties;
	}

	@Test
	void optimal() {
		final var filter = new FormatFilter(provider, true, Set.of(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT));
		properties.optimalTilingFeatures = new EnumMask<>(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT);
		assertEquals(true, filter.test(VkFormat.UNDEFINED));
	}

	@Test
	void linear() {
		final var filter = new FormatFilter(provider, false, Set.of(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT));
		properties.linearTilingFeatures = new EnumMask<>(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT);
		assertEquals(true, filter.test(VkFormat.UNDEFINED));
	}

	@Test
	void none() {
		final var filter = new FormatFilter(provider, false, Set.of(VkFormatFeatureFlags.DEPTH_STENCIL_ATTACHMENT));
		properties.linearTilingFeatures = new EnumMask<>();
		assertEquals(false, filter.test(VkFormat.UNDEFINED));
	}
}
