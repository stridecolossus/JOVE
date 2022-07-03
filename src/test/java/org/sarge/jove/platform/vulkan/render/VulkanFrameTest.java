package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.render.VulkanFrame.FrameRenderer;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class VulkanFrameTest extends AbstractVulkanTest {
	private VulkanFrame frame;
	private FrameRenderer renderer;
	private Swapchain swapchain;
	private Queue queue;

	@BeforeEach
	void before() {
		// Create swapchain
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);

		// Create presentation queue
		queue = new Queue(new Handle(1), new Family(0, 1, Set.of()));

		// Create frame
		renderer = mock(FrameRenderer.class);
		frame = new VulkanFrame(swapchain, queue, index -> renderer);
	}

	@Test
	void constructor() {
		assertNotNull(frame.available());
		assertNotNull(frame.ready());
		assertNotNull(frame.fence());
	}

	@Test
	void render() {
		// Init next swapchain image to be acquired
		when(swapchain.acquire(frame.available(), null)).thenReturn(1);

		// Render frame
		frame.render();

		// Check waits for previous frame to complete
		verify(swapchain).waitReady(1, frame.fence());

		// Check frame rendered
		verify(renderer).render(frame);

		// Check frame presented
		verify(swapchain).present(queue, 1, Set.of(frame.ready()));
	}

	@Test
	void destroy() {
		frame.destroy();
		assertEquals(true, frame.available().isDestroyed());
		assertEquals(true, frame.ready().isDestroyed());
		assertEquals(true, frame.fence().isDestroyed());
	}
}
