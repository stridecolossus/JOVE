package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.sarge.jove.util.TestHelper.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;

	@BeforeEach
	void before() {
		pipeline = new Pipeline(new Pointer(1), dev);
	}

	@Test
	void bind() {
		// Create command
		final Command cmd = pipeline.bind();
		assertNotNull(cmd);

		// Check bind pipeline
		final Handle buffer = new Handle(new Pointer(2));
		cmd.execute(lib, buffer);
		verify(lib).vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, pipeline.handle());
	}

	@Test
	void destroy() {
		final Handle handle = pipeline.handle();
		pipeline.destroy();
		verify(lib).vkDestroyPipeline(dev.handle(), handle, null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder builder;
		private PipelineLayout layout;
		private RenderPass pass;

		@BeforeEach
		void before() {
			builder = new Pipeline.Builder();
			layout = mock(PipelineLayout.class);
			pass = mock(RenderPass.class);
		}

		@Test
		void builders() {
			assertNotNull(builder.input());
			assertNotNull(builder.assembly());
			assertNotNull(builder.viewport());
			assertNotNull(builder.shader());
			assertNotNull(builder.rasterizer());
			assertNotNull(builder.blend());
		}

		@Test
		void build() {
			// Build pipeline
			pipeline = builder
					.layout(layout)
					.pass(pass)
					.viewport(new Dimensions(3, 4))
					.shader()
						.stage(VkShaderStageFlag.VERTEX)
						.shader(mock(Shader.class))
						.build()
					.build(dev);

			// Check pipeline
			assertNotNull(pipeline);

			// Check allocation
			final ArgumentCaptor<VkGraphicsPipelineCreateInfo[]> captor = ArgumentCaptor.forClass(VkGraphicsPipelineCreateInfo[].class);
			verify(lib).vkCreateGraphicsPipelines(eq(dev.handle()), isNull(), eq(1), captor.capture(), isNull(), isA(Pointer[].class));
			assertEquals(1, captor.getValue().length);

			// Check descriptor
			final VkGraphicsPipelineCreateInfo info = captor.getValue()[0];
			assertNotNull(info);
			assertEquals(null, info.basePipelineHandle);
			assertEquals(-1, info.basePipelineIndex);
			assertEquals(0, info.flags);

			// Check render pass
			// TODO
			assertEquals(0, info.subpass);

			// Check mandatory stage descriptors
			assertNotNull(info.pVertexInputState);
			assertNotNull(info.pInputAssemblyState);
			assertNotNull(info.pViewportState);
			assertNotNull(info.pRasterizationState);
			assertNotNull(info.pColorBlendState);

			// Check viewport stage
			assertEquals(1, info.pViewportState.viewportCount);
			assertEquals(1, info.pViewportState.scissorCount);

			// Check shader stage descriptor
			assertEquals(1, info.stageCount);
			assertNotNull(info.pStages);
		}

		private void addVertexShaderStage() {
			builder
				.shader()
					.stage(VkShaderStageFlag.VERTEX)
					.shader(mock(Shader.class))
				.build();
		}

		@Test
		void buildIncomplete() {
			// Check empty builder
			assertThrows(IllegalArgumentException.class, "pipeline layout", () -> builder.build(dev));

			// Add layout
			builder.layout(layout);
			assertThrows(IllegalArgumentException.class, "render pass", () -> builder.build(dev));

			// Add render-pass
			builder.pass(pass);
			assertThrows(IllegalStateException.class, "vertex shader", () -> builder.build(dev));

			// Add shader
			addVertexShaderStage();
			assertThrows(IllegalArgumentException.class, "viewports", () -> builder.build(dev));

			// Add viewport stage, should now build successfully
			builder.viewport(new Dimensions(3, 4));
			builder.build(dev);
		}

		@Test
		void duplicateShaderStage() {
			addVertexShaderStage();
			assertThrows(IllegalArgumentException.class, () -> addVertexShaderStage());
		}
	}
}
