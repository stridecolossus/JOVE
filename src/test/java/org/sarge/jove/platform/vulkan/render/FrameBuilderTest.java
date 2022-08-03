package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

public class FrameBuilderTest {
	private FrameBuilder builder;
	private FrameBuffer frame;
	private Buffer buffer;

	@BeforeEach
	void before() {
		frame = mock(FrameBuffer.class);
		buffer = mock(Buffer.class);
		builder = new FrameBuilder(n -> frame, () -> buffer, VkCommandBufferUsage.ONE_TIME_SUBMIT);
	}

	@Test
	void build() {
		// Init command to start render pass
		final Command begin = mock(Command.class);
		when(frame.begin()).thenReturn(begin);

		// Record render task
		final RenderSequence seq = mock(RenderSequence.class);
		builder.build(0, seq);

		// Check recorded commands
		verify(buffer).begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
		verify(buffer).add(begin);
		verify(seq).record(buffer);
		verify(buffer).add(FrameBuffer.END);
		verify(buffer).end();
	}
}
