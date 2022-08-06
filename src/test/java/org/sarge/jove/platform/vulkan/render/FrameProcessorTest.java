package org.sarge.jove.platform.vulkan.render;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.*;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.render.FrameProcessor.Listener;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.ReferenceFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

class FrameProcessorTest extends AbstractVulkanTest {
	private FrameProcessor proc;
	private Swapchain swapchain;
	private FrameBuilder builder;

	@BeforeEach
	void before() {
		// Create swapchain with two images
		final View attachment = mock(View.class);
		swapchain = mock(Swapchain.class);
		when(swapchain.device()).thenReturn(dev);
		when(swapchain.attachments()).thenReturn(List.of(attachment, attachment));

		// Init render task builder
		builder = mock(FrameBuilder.class);

		// TODO - needed to avoid semaphores with same handle being used twice, nasty?
		final ReferenceFactory factory = new ReferenceFactory() {
			private int index;

			@Override
			public IntByReference integer() {
				return null;
			}

			@Override
			public PointerByReference pointer() {
				return new PointerByReference(new Pointer(++index));
			}
		};
		when(dev.factory()).thenReturn(factory);

		// Create controller
		proc = new FrameProcessor(swapchain, builder, 2);
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

		final Listener listener = mock(Listener.class);
		proc.add(listener);

		// Render frame
		proc.render(seq);

		// TODO - how to test what the frame actually does?
		// verify(swapchain).acquire(null, null)

		verify(listener).frame(anyLong(), anyLong());
	}

	@Test
	void destroy() {
		proc.destroy();
	}
}
