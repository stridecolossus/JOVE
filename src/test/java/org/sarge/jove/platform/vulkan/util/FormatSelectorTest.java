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
		// Create properties for the format
		props = new VkFormatProperties();

		// Init mapper
		mapper = mock(Function.class);
		when(mapper.apply(FORMAT)).thenReturn(props);

		// Init test
		predicate = mock(Predicate.class);
		when(predicate.test(props)).thenReturn(true);

		// Create selector
		selector = new FormatSelector(mapper, predicate);
	}

	@Test
	void test() {
		assertEquals(true, selector.test(FORMAT));
	}

	@Test
	void select() {
		assertEquals(Optional.of(FORMAT), selector.select(List.of(FORMAT)));
	}

	@Test
	void selectNoneMatched() {
		when(predicate.test(props)).thenReturn(false);
		assertEquals(Optional.empty(), selector.select(List.of(FORMAT)));
	}

	@Nested
	class PredicateTests {
		@Test
		void optimal() {
			final Predicate<VkFormatProperties> predicate = FormatSelector.predicate(Set.of(FEATURE), true);
			props.optimalTilingFeatures = FEATURE.value();
			assertNotNull(predicate);
			assertEquals(true, predicate.test(props));
		}

		@Test
		void linear() {
			final Predicate<VkFormatProperties> predicate = FormatSelector.predicate(Set.of(FEATURE), false);
			props.linearTilingFeatures = FEATURE.value();
			assertNotNull(predicate);
			assertEquals(true, predicate.test(props));
		}

		@Test
		void none() {
			final Predicate<VkFormatProperties> predicate = FormatSelector.predicate(Set.of(FEATURE), true);
			assertNotNull(predicate);
			assertEquals(false, predicate.test(props));
		}
	}
}
