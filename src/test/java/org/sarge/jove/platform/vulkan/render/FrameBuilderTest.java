package org.sarge.jove.platform.vulkan.render;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Buffer.Recorder;

public class FrameBuilderTest {
	private FrameBuilder builder;
	private FrameBuffer frame;
	private Buffer buffer;
	private Recorder recorder;

	@BeforeEach
	void before() {
		frame = mock(FrameBuffer.class);
		builder = new FrameBuilder(n -> frame, () -> buffer, VkCommandBufferUsage.ONE_TIME_SUBMIT);
		recorder = mock(Recorder.class);
		buffer = mock(Buffer.class);
		when(buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)).thenReturn(recorder);
		when(recorder.add(any(Command.class))).thenReturn(recorder);
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
		verify(recorder).add(begin);
		verify(seq).record(recorder);
		verify(recorder).add(FrameBuffer.END);
		verify(recorder).end();
	}
}
