package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.function.Predicate;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

class FormatSelectorTest {
	private FormatSelector selector;

	@BeforeEach
	void before() {
		selector = new FormatSelector(_ -> new VkFormatProperties());
	}

	@Test
	void select() {
		assertEquals(Optional.of(VkFormat.UNDEFINED), selector.select(List.of(VkFormat.UNDEFINED), _ -> true));
	}

	@Nested
	class FilterTest {
		private VkFormatProperties match, empty;

		@BeforeEach
		void before() {
			match = new VkFormatProperties();
			empty = new VkFormatProperties();
		}

    	@Test
    	void optimal() {
    		final Predicate<VkFormatProperties> filter = FormatSelector.filter(true, Set.of(VkFormatFeature.COLOR_ATTACHMENT));
    		match.optimalTilingFeatures = new EnumMask<>(VkFormatFeature.COLOR_ATTACHMENT);
    		assertEquals(true, filter.test(match));
    		assertEquals(false, filter.test(empty));
    	}

    	@Test
    	void linear() {
    		final Predicate<VkFormatProperties> filter = FormatSelector.filter(false, Set.of(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT));
    		match.linearTilingFeatures = new EnumMask<>(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT);
    		assertEquals(true, filter.test(match));
    		assertEquals(false, filter.test(empty));
    	}
    }
}
