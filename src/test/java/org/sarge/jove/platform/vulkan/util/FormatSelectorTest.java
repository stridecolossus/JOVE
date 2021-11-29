package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatFeature;
import org.sarge.jove.platform.vulkan.VkFormatProperties;

public class FormatSelectorTest {
	private static final VkFormat FORMAT = VkFormat.R16_UNORM;
	private static final VkFormatFeature FEATURE = VkFormatFeature.DEPTH_STENCIL_ATTACHMENT;

	private FormatSelector selector;
	private Function<VkFormat, VkFormatProperties> mapper;
	private Predicate<VkFormatProperties> predicate;
	private VkFormatProperties props;

	@BeforeEach
	void before() {
		mapper = mock(Function.class);
		predicate = mock(Predicate.class);
		selector = new FormatSelector(mapper, predicate);
		props = new VkFormatProperties();
	}

	@Test
	void select() {
		when(mapper.apply(FORMAT)).thenReturn(props);
		when(predicate.test(props)).thenReturn(true);
		assertEquals(Optional.of(FORMAT), selector.select(List.of(FORMAT)));
	}

	@Test
	void selectNoneMatched() {
		when(mapper.apply(FORMAT)).thenReturn(props);
		assertEquals(Optional.empty(), selector.select(List.of(FORMAT)));
	}

	@Nested
	class FormatFeaturePredicateTests {
		private VkFormatProperties other;

		@BeforeEach
		void before() {
			other = new VkFormatProperties();
		}

		@Test
		void optimal() {
			final Predicate<VkFormatProperties> optimal = FormatSelector.feature(Set.of(FEATURE), true);
			assertNotNull(optimal);
			props.optimalTilingFeatures = FEATURE.value();
			assertEquals(true, optimal.test(props));
			assertEquals(false, optimal.test(other));
		}

		@Test
		void linear() {
			final Predicate<VkFormatProperties> linear = FormatSelector.feature(Set.of(FEATURE), false);
			assertNotNull(linear);
			props.linearTilingFeatures = FEATURE.value();
			assertEquals(true, linear.test(props));
			assertEquals(false, linear.test(other));
		}
	}
}
