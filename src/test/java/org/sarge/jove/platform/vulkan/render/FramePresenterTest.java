package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.render.FramePresenter.Frame;
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

		presenter = new FramePresenter(swapchain, builder, 2);
	}

	@Test
	void next() {
		final Frame frame = presenter.next();
		assertNotNull(frame);
	}

	@Test
	void destroy() {
		presenter.destroy();
	}

	@Nested
	class FrameTest {
		private Frame frame;
		private Semaphore available, ready;
		private Fence fence;

		@BeforeEach
		void before() {
			available = mock(Semaphore.class);
			ready = mock(Semaphore.class);
			fence = mock(Fence.class);
			when(available.handle()).thenReturn(new Handle(1));
			when(ready.handle()).thenReturn(new Handle(2));
			when(fence.handle()).thenReturn(new Handle(3));
			frame = presenter.new Frame(available, ready, fence);
		}

		@Test
		void render() {
			// Init command buffer
			final Pool pool = mock(Pool.class);
			final Buffer buffer = mock(Buffer.class);
			when(buffer.handle()).thenReturn(new Handle(4));
			when(buffer.isReady()).thenReturn(true);
			when(buffer.pool()).thenReturn(pool);
			when(pool.device()).thenReturn(dev);

			// Create presentation queue
			final Queue queue = new Queue(new Handle(5), new Family(1, 2, Set.of()));
			when(pool.queue()).thenReturn(queue);

			// Init frame builder
			final RenderSequence seq = mock(RenderSequence.class);
			when(builder.build(0, seq)).thenReturn(buffer);

			// Render frame
			frame.render(seq);

			// Check synchronisation
			verify(fence, times(2)).waitReady();
			verify(fence).reset();

			// Check next frame buffer is acquired
			verify(swapchain).acquire(available, null);

			// Check frame is presented
			verify(swapchain).present(queue, 0, ready);
		}

		@Test
		void destroy() {
			frame.destroy();
			verify(available).destroy();
			verify(ready).destroy();
			verify(fence).destroy();
		}
	}
}
