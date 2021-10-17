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
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;
	private PipelineLayout layout;

	@BeforeEach
	void before() {
		layout = mock(PipelineLayout.class);
		pipeline = new Pipeline(new Pointer(1), dev, layout);
	}

	@Test
	void constructor() {
		assertEquals(layout, pipeline.layout());
		assertEquals(false, pipeline.isDestroyed());
	}

	@Test
	void bind() {
		// Create command
		final Command cmd = pipeline.bind();
		assertNotNull(cmd);

		// Check bind pipeline
		final Command.Buffer cb = mock(Command.Buffer.class);
		cmd.execute(lib, cb);
		verify(lib).vkCmdBindPipeline(cb, VkPipelineBindPoint.GRAPHICS, pipeline);
	}

	@Test
	void close() {
		pipeline.close();
		verify(lib).vkDestroyPipeline(dev, pipeline, null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder builder;
		private RenderPass pass;
		private Rectangle viewport;

		@BeforeEach
		void before() {
			builder = new Pipeline.Builder();
			pass = mock(RenderPass.class);
			viewport = new Rectangle(new Dimensions(3, 4));
		}

		@Test
		void builders() {
			assertNotNull(builder.input());
			assertNotNull(builder.assembly());
			assertNotNull(builder.viewport());
			assertNotNull(builder.rasterizer());
			assertNotNull(builder.blend());
		}

		@Test
		void build() {
			// Build pipeline
			pipeline = builder
					.layout(layout)
					.pass(pass)
					.viewport()
						.viewport(viewport)
						.scissor(viewport)
						.build()
					.shader(VkShaderStage.VERTEX)
						.shader(mock(Shader.class))
						.build()
					.build(dev);

			// Check pipeline
			assertNotNull(pipeline);

			// Check allocation
			final ArgumentCaptor<VkGraphicsPipelineCreateInfo[]> captor = ArgumentCaptor.forClass(VkGraphicsPipelineCreateInfo[].class);
			verify(lib).vkCreateGraphicsPipelines(eq(dev), isNull(), eq(1), captor.capture(), isNull(), isA(Pointer[].class));
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
				.shader(VkShaderStage.VERTEX)
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
			builder
				.viewport()
					.viewport(viewport)
					.scissor(viewport)
					.build()
				.build(dev);
		}

		@Test
		void duplicateShaderStage() {
			addVertexShaderStage();
			assertThrows(IllegalArgumentException.class, () -> addVertexShaderStage());
		}
	}

	@Nested
	class ShaderStageBuilderTests {
		// TODO
	}
}
