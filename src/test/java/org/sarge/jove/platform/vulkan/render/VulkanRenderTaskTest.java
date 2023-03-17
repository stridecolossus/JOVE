package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command;

class VulkanRenderTaskTest {
	private VulkanRenderTask task;
	private FrameBuffer buffer;
	private FrameSelector selector;
	private FrameComposer composer;
	private Swapchain swapchain;

	@BeforeEach
	void before() {
		buffer = mock(FrameBuffer.class);
		selector = mock(FrameSelector.class);
		composer = mock(FrameComposer.class);
		swapchain = mock(Swapchain.class);
		task = new VulkanRenderTask(List.of(buffer, buffer), selector, composer, swapchain);
	}

	@Test
	void render() {
		final var frame = mock(VulkanFrame.class);
		final var render = mock(Command.Buffer.class);
		when(frame.acquire(swapchain)).thenReturn(1);
		when(selector.frame()).thenReturn(frame);
		when(composer.compose(buffer)).thenReturn(render);
		task.render();
		verify(frame).present(render, swapchain);
	}
}
