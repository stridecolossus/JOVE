package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

@Disabled("This is way too messy, class needs to be simpler/template?")
public class FramePresenterTest extends AbstractVulkanTest {
	private FramePresenter presenter;
	private FrameSet frames;
	private Swapchain swapchain;
	private FrameBuffer frame;
	private RenderSequence seq;

	@BeforeEach
	void before() {
		// Create swapchain
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);

		// Create frame buffers set
		frames = mock(FrameSet.class);
		when(frames.swapchain()).thenReturn(swapchain);

		// Create frame buffer
		frame = mock(FrameBuffer.class);
		when(frames.buffer(0)).thenReturn(frame);

		// Create rendering sequence
		seq = mock(RenderSequence.class);

		// Create presenter
		presenter = new FramePresenter(frames, seq);
	}

	@Test
	void render() {
		final Command.Pool pool = mock(Command.Pool.class);
		final Queue queue = new Queue(new Handle(1), new Queue.Family(1, 2, Set.of()));
		when(pool.queue()).thenReturn(queue);

		final Command.Buffer buffer = mock(Command.Buffer.class);
		when(buffer.pool()).thenReturn(pool);
		when(buffer.isReady()).thenReturn(true);

		when(seq.build(frame)).thenReturn(buffer);

		presenter.render();
	}

	@Test
	void destroy() {
		presenter.destroy();
	}
}
