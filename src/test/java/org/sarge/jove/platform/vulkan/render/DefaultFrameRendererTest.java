package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class DefaultFrameRendererTest extends AbstractVulkanTest {
	private DefaultFrameRenderer renderer;
	private FrameBuilder builder;
	private Buffer buffer;
	private Queue queue;

	@BeforeEach
	void before() {
		// Create work queue
		queue = new Queue(new Handle(1), new Family(0, 1, Set.of()));

		// Create frame builder
		buffer = mock(Buffer.class);
		builder = mock(FrameBuilder.class);
		when(builder.build()).thenReturn(buffer);

		// Create renderer
		renderer = new DefaultFrameRenderer(builder, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT);
	}

	private Semaphore semaphore() {
		final Semaphore semaphore = mock(Semaphore.class);
		when(semaphore.handle()).thenReturn(new Handle(2));
		return semaphore;
	}

	@Test
	void render() {
		// Init frame sync
		final VulkanFrame frame = mock(VulkanFrame.class);
		final Semaphore available = semaphore();
		final Semaphore ready = semaphore();
		final Fence fence = mock(Fence.class);
		when(frame.available()).thenReturn(available);
		when(frame.ready()).thenReturn(ready);
		when(frame.fence()).thenReturn(fence);

		// Create command pool
		final Pool pool = mock(Pool.class);
		when(pool.queue()).thenReturn(queue);
		when(pool.device()).thenReturn(dev);

		// Init buffer
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(1));
		when(buffer.isReady()).thenReturn(true);

		// Render frame
		renderer.render(frame);

		// TODO
//		// Check render submission
//		final Batch expected = new Work.Builder(pool)
//				.add(buffer)
//				.wait(frame.available(), VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
//				.signal(frame.ready())
//				.build()
//				.batch();
//		verify(pool).submit(expected, fence);
	}
}
