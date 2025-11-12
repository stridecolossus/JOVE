package org.sarge.jove.platform.vulkan.render;

import org.junit.jupiter.api.BeforeEach;

class VulkanRenderTaskTest {
	private VulkanRenderTask task;
	private Swapchain swapchain;
	private FrameBuffer.Group group;
	private FrameComposer composer;

	@BeforeEach
	void before() {
		//task = new VulkanRenderTask(swapchain, group, composer);
	}


}

//
//		composer = mock(FrameComposer.class);
//		adapter = mock(SwapchainAdapter.class);
//		frame = mock(VulkanFrame.class);
//		swapchain = mock(Swapchain.class);
//		when(adapter.swapchain()).thenReturn(swapchain);
//		when(frame.acquire(swapchain)).thenReturn(2);
//		task = new VulkanRenderTask(composer, adapter, new VulkanFrame[]{frame});
//	}
//
//	@Test
//	void render() {
//		// Create a frame buffer
//		final FrameBuffer buffer = mock(FrameBuffer.class);
//		when(adapter.buffer(2)).thenReturn(buffer);
//
//		// Init render sequence
//		final var render = mock(Command.CommandBuffer.class);
//		when(composer.compose(2, buffer)).thenReturn(render);
//
//		// Render frame
//		task.render();
//
//		// Check frame is presented to the swapchain
//		verify(frame).present(render, 2, swapchain);
//	}
//
//	@Test
//	void cycle() {
//		// TODO - how to test this?
//	}
//
//	@Test
//	void recreate() {
//		when(frame.acquire(swapchain)).thenThrow(SwapchainInvalidated.class);
//		task.render();
//		verify(adapter).recreate();
//		verifyNoMoreInteractions(swapchain);
//	}
//
//	@Test
//	void destroy() {
//		task.destroy();
//		verify(frame).destroy();
//	}
//}
