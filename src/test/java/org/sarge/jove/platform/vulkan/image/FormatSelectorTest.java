package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Predicate;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.jove.util.BitMask;

public class FormatSelectorTest {
	private static final VkFormat FORMAT = VkFormat.UNDEFINED;

	private FormatSelector selector;
	private PhysicalDevice dev;
	private VkFormatProperties props;

	@BeforeEach
	void before() {
		props = new VkFormatProperties();
		dev = mock(PhysicalDevice.class);
		selector = new FormatSelector(dev, p -> p == props);
	}

	@Test
	void select() {
		when(dev.properties(FORMAT)).thenReturn(props);
		assertEquals(Optional.of(FORMAT), selector.select(FORMAT));
	}

	@Test
	void candidates() {
		when(dev.properties(FORMAT)).thenReturn(props);
		assertEquals(Optional.of(FORMAT), selector.select(VkFormat.A1R5G5B5_UNORM_PACK16, FORMAT));
	}

	@Test
	void none() {
		assertEquals(Optional.empty(), selector.select(FORMAT));
	}

	@Nested
	class FilterTests {
		private static final VkFormatFeature FEATURE = VkFormatFeature.DEPTH_STENCIL_ATTACHMENT;

		@Test
		void optimal() {
			final Predicate<VkFormatProperties> filter = FormatSelector.filter(true, Set.of(FEATURE));
			props.optimalTilingFeatures = BitMask.reduce(FEATURE);
			assertEquals(true, filter.test(props));
		}

		@Test
		void linear() {
			final Predicate<VkFormatProperties> filter = FormatSelector.filter(false, Set.of(FEATURE));
			props.linearTilingFeatures = BitMask.reduce(FEATURE);
			assertEquals(true, filter.test(props));
		}

		@Test
		void unmatched() {
			props.linearTilingFeatures = new BitMask<>(0);
			props.optimalTilingFeatures = new BitMask<>(0);
			assertEquals(false, FormatSelector.filter(false, Set.of(FEATURE)).test(props));
			assertEquals(false, FormatSelector.filter(true, Set.of(FEATURE)).test(props));
		}
	}
}
