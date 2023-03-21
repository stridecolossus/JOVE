package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command;

class VulkanRenderTaskTest {
	private VulkanRenderTask task;
	private FrameBuffer buffer;
	private VulkanFrame frame;
	private FrameComposer composer;
	private Swapchain swapchain;

	@BeforeEach
	void before() {
		buffer = mock(FrameBuffer.class);
		frame = mock(VulkanFrame.class);
		composer = mock(FrameComposer.class);
		swapchain = mock(Swapchain.class);
		task = new VulkanRenderTask(List.of(buffer), new VulkanFrame[]{frame}, composer, swapchain);
	}

	@Test
	void render() {
		final var render = mock(Command.Buffer.class);
		when(frame.acquire(swapchain)).thenReturn(0);
		when(composer.compose(0, buffer)).thenReturn(render);
		task.render();
		verify(frame).present(render);
	}
}
