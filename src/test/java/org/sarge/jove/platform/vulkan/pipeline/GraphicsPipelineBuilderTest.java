package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.ViewportStage.Viewport;
import org.sarge.jove.platform.vulkan.render.*;

class GraphicsPipelineBuilderTest {
	private static class MockPipelineLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines) {
			assertNotNull(device);
			assertEquals(null, pipelineCache);
			assertEquals(1, createInfoCount);

//			pCreateInfos[0].

			assertEquals(1, pPipelines.length);
			pPipelines[0] = new Handle(4);
			return VkResult.SUCCESS;
		}
	}

	private GraphicsPipelineBuilder builder;
	private LogicalDevice device;
	private MockPipelineLibrary library;
	private PipelineLayout layout;
	private RenderPass pass;
	private ProgrammableShaderStage shader;

	@BeforeEach
	void before() {
		library = new MockPipelineLibrary();
		device = new MockLogicalDevice(library);
		layout = new PipelineLayout(new Handle(5), device, null);
		pass = new MockRenderPass(device);
		shader = new ProgrammableShaderStage(VkShaderStage.VERTEX, new MockShader(device), "main", new SpecialisationConstants(Map.of())); // TODO - optional constants
		builder = new GraphicsPipelineBuilder();
	}

	@Test
	void build() {
		builder
				.layout(layout)
				.pass(pass)
				.shader(shader);

		// TODO - just rectangle
		// TODO - does NOT return builder, shader stage doesn't!!!
		builder.viewport().viewportAndScissor(new Viewport(new Rectangle(0, 0, 640, 480)));

		final Pipeline pipeline = builder.build(device);

		assertEquals(false, pipeline.isParent());

	}

	/**
	 * TODO
	 * this needs a lot of refactoring
	 * sub builders really suck
	 */
}
