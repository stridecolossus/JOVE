package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;

class VulkanFrameTest {
	private DeviceContext dev;
	private VulkanFrame frame;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		frame = new VulkanFrame(dev);
	}

	@Test
	void render() {
		// Create swapchain
		final var swapchain = mock(Swapchain.class);
		when(swapchain.acquire(frame.available(), null)).thenReturn(1);

		// Create render sequence
		final var queue = new WorkQueue(new Handle(1), new WorkQueue.Family(1, 2, Set.of()));
		final var pool = Command.Pool.create(dev, queue);
		final var render = mock(Command.Buffer.class);
		when(render.pool()).thenReturn(pool);
		when(render.isReady()).thenReturn(true);

		// Create frame composer
		final var composer = mock(FrameComposer.class);
		when(composer.compose(1)).thenReturn(render);

		// Render frame
		frame.render(composer, swapchain);
		verify(swapchain).present(queue, 1, frame.ready());
	}

	@Test
	void destroy() {
		frame.destroy();
	}
}
