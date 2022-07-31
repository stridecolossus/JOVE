package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.function.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

public class RenderSequenceTest {
	private RenderSequence seq;
	private Supplier<Buffer> factory;
	private Consumer<Buffer> recorder;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		factory = mock(Supplier.class);
		recorder = mock(Consumer.class);
		seq = new RenderSequence(factory, recorder, VkCommandBufferUsage.ONE_TIME_SUBMIT);
	}

	@Test
	void build() {
		// Create the target frame buffer
		final FrameBuffer frame = mock(FrameBuffer.class);
		final Command begin = mock(Command.class);
		when(frame.begin()).thenReturn(begin);

		// Create command buffer
		final Buffer buffer = mock(Buffer.class);
		when(factory.get()).thenReturn(buffer);

		// Build rendering sequence
		assertEquals(buffer, seq.build(frame));
		verify(buffer).begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
		verify(buffer).add(begin);
		verify(recorder).accept(buffer);
		verify(buffer).add(FrameBuffer.END);
		verify(buffer).end();
	}
}
