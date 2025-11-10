package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.core.*;

class PipelineTest {
	private static class MockPipelineLibrary extends MockVulkanLibrary {
		private boolean bound;

		@Override
		public void vkCmdBindPipeline(Command.Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline) {
			assertEquals(VkPipelineBindPoint.GRAPHICS, pipelineBindPoint);
			assertNotNull(pipeline);
			bound = true;
		}
	}

	private Pipeline pipeline;
	private PipelineLayout layout;
	private LogicalDevice device;
	private MockPipelineLibrary library;

	@BeforeEach
	void before() {
		library = new MockPipelineLibrary();
		device = new MockLogicalDevice(library);
		layout = new PipelineLayout(new Handle(2), device, null);
		pipeline = new Pipeline(new Handle(3), device, VkPipelineBindPoint.GRAPHICS, layout, true);
	}

	@Test
	void bind() {
		final Command bind = pipeline.bind();
		bind.execute(null);
		assertEquals(true, library.bound);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		assertEquals(true, pipeline.isDestroyed());
	}
}
