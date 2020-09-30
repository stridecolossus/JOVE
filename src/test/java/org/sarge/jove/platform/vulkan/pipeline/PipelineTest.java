package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.core.Shader;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class PipelineTest extends AbstractVulkanTest {
	private Pipeline pipeline;

	@BeforeEach
	void before() {
		pipeline = new Pipeline(new Pointer(42), dev);
	}

	@Test
	void destroy() {
		pipeline.destroy();
		verify(lib).vkDestroyPipeline(dev.handle(), new Handle(new Pointer(42)), null);
	}

	@Nested
	class BuilderTests {
		private Pipeline.Builder builder;
		private Rectangle rect;

		@BeforeEach
		void before() {
			builder = new Pipeline.Builder(dev);
			rect = new Rectangle(new Dimensions(3, 4));
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
					.viewport()
						.viewport(rect)
						.scissor(rect)
						.build()
					.shader()
						.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
						.shader(mock(Shader.class))
						.build()
					.build();

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

			// Check shader stage descriptor
			assertEquals(1, info.stageCount);
			assertNotNull(info.pStages);
		}

		private void addShaderStage() {
			builder
				.shader()
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.shader(mock(Shader.class))
				.build();
		}

		@Test
		void buildRequiresVertexShaderStage() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void buildRequiresViewportStage() {
			addShaderStage();
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}

		@Test
		void duplicateShaderStage() {
			addShaderStage();
			assertThrows(IllegalArgumentException.class, () -> addShaderStage());
		}
	}
}
