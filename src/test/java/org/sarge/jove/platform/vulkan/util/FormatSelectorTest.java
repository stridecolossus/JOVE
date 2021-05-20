package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sarge.jove.platform.vulkan.VkFormatFeature.DEPTH_STENCIL_ATTACHMENT;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkFormatFeature;
import org.sarge.jove.platform.vulkan.VkFormatProperties;

public class FormatSelectorTest {
	private static final Set<VkFormatFeature> FEATURES = Set.of(DEPTH_STENCIL_ATTACHMENT);
	private static final VkFormat FORMAT = VkFormat.D32_SFLOAT;

	private FormatSelector selector;
	private Function<VkFormat, VkFormatProperties> func;
	private VkFormatProperties props;

	@BeforeEach
	void before() {
		func = mock(Function.class);
		selector = new FormatSelector(func);
		props = new VkFormatProperties();
		when(func.apply(FORMAT)).thenReturn(props);
	}

	@Test
	void select() {
		props.linearTilingFeatures = DEPTH_STENCIL_ATTACHMENT.value();
		assertEquals(Optional.of(FORMAT), selector.select(false, FEATURES, List.of(FORMAT)));
		verify(func).apply(FORMAT);
	}

	@Test
	void selectCached() {
		selector.select(false, FEATURES, List.of(FORMAT));
		selector.select(false, FEATURES, List.of(FORMAT));
		verify(func, atMostOnce()).apply(FORMAT);
	}

	@Test
	void selectOptimal() {
		props.optimalTilingFeatures = DEPTH_STENCIL_ATTACHMENT.value();
		assertEquals(Optional.of(FORMAT), selector.select(true, FEATURES, List.of(FORMAT)));
		verify(func).apply(FORMAT);
	}

	@Test
	void selectNotMatched() {
		assertEquals(Optional.empty(), selector.select(false, FEATURES, List.of(FORMAT)));
	}
}
