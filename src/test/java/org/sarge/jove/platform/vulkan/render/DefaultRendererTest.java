package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class DefaultRendererTest extends AbstractVulkanTest {
	private DefaultRenderer renderer;
	private Buffer buffer;
	private Queue queue;

	@BeforeEach
	void before() {
		queue = new Queue(new Handle(1), new Family(0, 1, Set.of()));
		buffer = mock(Buffer.class);
		renderer = new DefaultRenderer(ignored -> Stream.of(buffer));
	}

	private Semaphore semaphore() {
		final Semaphore semaphore = mock(Semaphore.class);
		when(semaphore.handle()).thenReturn(new Handle(2));
		return semaphore;
	}

	@Test
	void renderer() {
		// Create command pool
		final Pool pool = mock(Pool.class);
		when(pool.queue()).thenReturn(queue);
		when(pool.device()).thenReturn(dev);

		// Init buffer
		when(buffer.pool()).thenReturn(pool);
		when(buffer.handle()).thenReturn(new Handle(1));
		when(buffer.isReady()).thenReturn(true);

		// Init synchronisation
		final Semaphore available = semaphore();
		final Semaphore ready = semaphore();
		final VulkanFrame frame = mock(VulkanFrame.class);
		when(frame.available()).thenReturn(available);
		when(frame.ready()).thenReturn(ready);

		// Render frame
		renderer.render(1, frame);

		// TODO
	}
}
