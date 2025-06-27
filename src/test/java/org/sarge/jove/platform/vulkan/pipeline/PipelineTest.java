package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.CommandBuffer;

class PipelineTest {
	private Pipeline pipeline;
	private PipelineLayout layout;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		layout = new PipelineLayout(new Handle(1), dev, new PushConstant(List.of()));
		pipeline = new Pipeline(new Handle(2), dev, VkPipelineBindPoint.GRAPHICS, layout, true);
	}

	@Test
	void isParent() {
		assertEquals(true, pipeline.isParent());
	}

	@Test
	void bind() {
		final Command bind = pipeline.bind();
		final VulkanLibrary lib = dev.vulkan().library();
		final CommandBuffer buffer = new MockCommandBuffer();
		bind.execute(lib, buffer);
		// TODO
		// return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, type, Pipeline.this);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		// TODO
	}
}
