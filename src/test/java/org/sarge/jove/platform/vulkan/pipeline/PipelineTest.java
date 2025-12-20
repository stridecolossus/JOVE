package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.Mockery;

class PipelineTest {
	private Pipeline pipeline;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(Pipeline.Library.class);
		final var device = new MockLogicalDevice(mockery.proxy());
		pipeline = new Pipeline(new Handle(3), device, VkPipelineBindPoint.GRAPHICS, new MockPipelineLayout(), true);
	}

	@Test
	void bind() {
		final Command bind = pipeline.bind();
		bind.execute(null);
		assertEquals(1, mockery.mock("vkCmdBindPipeline").count());
	}

	@Test
	void destroy() {
		pipeline.destroy();
		assertEquals(true, pipeline.isDestroyed());
		assertEquals(1, mockery.mock("vkDestroyPipeline").count());
	}
}
