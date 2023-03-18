package org.sarge.jove.platform.vulkan.render;

import java.util.*;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.*;

class FrameComposerTest {
	private DeviceContext dev;
	private FrameComposer composer;
	private Pool pool;
	private Supplier<List<Buffer>> seq;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();

		// Create a command pool
		final WorkQueue queue = new WorkQueue(new Handle(1), new WorkQueue.Family(1, 2, Set.of(VkQueueFlag.GRAPHICS)));
		pool = Command.Pool.create(dev, queue);

		// Init render sequence factory
		seq = () -> List.of();

		// Create composer
		composer = new FrameComposer(pool, seq);
	}

	@Test
	void compose() {
//		// Create the frame buffer
//		final var frame = mock(FrameBuffer.class);
//		when(frame.begin(VkSubpassContents.SECONDARY_COMMAND_BUFFERS)).thenReturn(mock(Command.class));
//
//		// Init the render sequence
//		when(seq.get()).thenReturn(List.of());
//
//		// Compose the render task
//		assertEquals(render, composer.compose(frame));
//		verify(frame).begin();
//		verify(seq).get();
	}

	@Test
	void flags() {
		final VkCommandBufferUsage[] flags = {VkCommandBufferUsage.ONE_TIME_SUBMIT};
		composer.flags(flags);
	}
}
