package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.FrameBuilder.Recorder;

public class FrameBuilderTest {
	private FrameBuilder builder;
	private Buffer buffer;
	private Recorder recorder;

	@BeforeEach
	void before() {
		// Init command sequence
		buffer = mock(Buffer.class);

		// Create recorder
		recorder = spy(Recorder.class);

		// Create builder
		builder = new FrameBuilder(() -> buffer, recorder);
	}

	@Test
	void build() {
		assertEquals(buffer, builder.build());
		verify(recorder).record(buffer);
	}

	@Test
	void renderPass() {
		// Create frame buffer
		final Command begin = mock(Command.class);
		final FrameBuffer frame = mock(FrameBuffer.class);
		when(frame.begin()).thenReturn(begin);

		// Wrap command sequence with a render pass
		final Recorder pass = recorder.render(frame);
		assertNotNull(pass);
		pass.record(buffer);

		// Check render pass
		verify(buffer).begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
		verify(buffer).add(begin);
		verify(buffer).add(FrameBuffer.END);
		verify(buffer).end();

		// Check delegated to recorder
		verify(recorder).record(buffer);
	}
}
