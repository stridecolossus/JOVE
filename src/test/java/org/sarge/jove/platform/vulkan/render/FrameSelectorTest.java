package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;

import org.junit.jupiter.api.*;

class FrameSelectorTest {
	private FrameSelector selector;
	private VulkanFrame one, two;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		final Supplier<VulkanFrame> factory = mock(Supplier.class);
		one = mock(VulkanFrame.class);
		two = mock(VulkanFrame.class);
		when(factory.get()).thenReturn(one, two);
		selector = FrameSelector.flight(2, factory);
	}

	@Test
	void frame() {
		assertEquals(one, selector.frame());
		assertEquals(two, selector.frame());
		assertEquals(one, selector.frame());
	}

	@Test
	void destroy() {
		selector.destroy();
		verify(one).destroy();
		verify(two).destroy();
	}
}
