package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

class FramePresenterTest extends AbstractVulkanTest {
	private FramePresenter presenter;
	private Swapchain swapchain;
	private FrameBuilder builder;

	@BeforeEach
	void before() {
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);

		builder = mock(FrameBuilder.class);

		presenter = new FramePresenter(swapchain, builder);
	}

	@Test
	void render() {
		final RenderSequence seq = mock(RenderSequence.class);
		final Buffer buffer = mock(Buffer.class);
		when(builder.build(0, seq)).thenReturn(buffer);

		//presenter.render(seq);
		//verify(swapchain).acquire(null, null);
	}

	@Test
	void destroy() {
		presenter.destroy();
	}
}
