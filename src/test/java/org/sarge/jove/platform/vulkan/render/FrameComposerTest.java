package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkSubpassContents;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.FrameComposer.BufferPolicy;

class FrameComposerTest {
	private static class ComposedFramebuffer extends MockFramebuffer {
		boolean begin;
		boolean end;

		@Override
		public Command begin(VkSubpassContents contents) {
			begin = true;
			assertEquals(VkSubpassContents.INLINE, contents);
			return super.begin(contents);
		}

		@Override
		public Command end() {
			end = true;
			return super.end();
		}
	}

	private FrameComposer composer;
	private MockCommandPool pool;
	private AtomicReference<Buffer> sequence;
	private ComposedFramebuffer framebuffer;

	@BeforeEach
	void before() {
		pool = new MockCommandPool();
		sequence = new AtomicReference<>();
		framebuffer = new ComposedFramebuffer();
		composer = new FrameComposer(pool, BufferPolicy.DEFAULT, sequence::set);
	}

	@Test
	void compose() {
		final Buffer buffer = composer.compose(1, framebuffer);
		assertEquals(true, buffer.isReady());
		assertEquals(true, buffer.isPrimary());
		assertEquals(pool, buffer.pool());
		// TODO - how to check buffer was created with correct flags? => would need to check vkBeginCommandBuffer()
		assertEquals(buffer, sequence.get());
		assertEquals(true, framebuffer.begin);
		assertEquals(true, framebuffer.end);
	}
}
