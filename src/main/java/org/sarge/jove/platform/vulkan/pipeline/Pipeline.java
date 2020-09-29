package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;

/**
 * A <i>pipeline</i> specifies the sequence of operations for graphics rendering.
 * @author Sarge
 */
public class Pipeline {
	private final Handle handle;
	private final LogicalDevice dev;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 */
	Pipeline(Pointer handle, LogicalDevice dev) {
		this.handle = new Handle(handle);
		this.dev = notNull(dev);
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, handle);
	}

	/**
	 * Destroys this pipeline.
	 */
	public void destroy() {
		dev.library().vkDestroyPipeline(dev.handle(), handle, null);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Builder for a pipeline.
	 */
	public static class Builder {
		private final LogicalDevice dev;
//		private Layout layout;
//		private RenderPass pass;
		private final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();
		private final Map<VkShaderStageFlag, VkPipelineShaderStageCreateInfo> shaders = new HashMap<>();
		// TODO - these should be cleared at build()?

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			this.pipeline.pVertexInputState = new VkPipelineVertexInputStateCreateInfo();
			this.pipeline.pInputAssemblyState = new VkPipelineInputAssemblyStateCreateInfo();
			this.pipeline.pRasterizationState = new VkPipelineRasterizationStateCreateInfo();
			this.pipeline.pMultisampleState = new VkPipelineMultisampleStateCreateInfo();
			this.pipeline.pColorBlendState = ColourBlendStageBuilder.create();
		}

		/**
		 * @return Builder for the vertex input stage
		 */
		public VertexInputStageBuilder input() {
			return new VertexInputStageBuilder() {
				@Override
				public Builder build() {
					pipeline.pVertexInputState = buildLocal();
					return Builder.this;
				}
			};
		}

		// TODO - input assembly
		// TODO - tesselation

		/**
		 * @return Builder for the viewport stage
		 */
		public ViewportStageBuilder viewport() {
			return new ViewportStageBuilder() {
				@Override
				public Builder build() {
					pipeline.pViewportState = buildLocal();
					return Builder.this;
				}
			};
		}

		// TODO - rasterization, multisample, depth stencil

		/**
		 * @return Builder for the colour-blend stage
		 */
		public ColourBlendStageBuilder blend() {
			return new ColourBlendStageBuilder() {
				@Override
				public Builder build() {
					pipeline.pColorBlendState = buildLocal();
					return Builder.this;
				}
			};
		}

		// TODO - dynamic

		/**
		 * @return Builder for a shader stage
		 * @throws IllegalArgumentException for a duplicate stage
		 */
		public ShaderStageBuilder shader() {
			return new ShaderStageBuilder() {
				@Override
				public Builder build() {
					final var info = buildLocal();
					if(shaders.containsKey(info.stage)) throw new IllegalArgumentException("Duplicate shader stage: " + info.stage);
					shaders.put(info.stage, info);
					return Builder.this;
				}
			};
		}

		/**
		 * Constructs this pipeline.
		 * @return New pipeline
		 * @throws IllegalArgumentException if any of the following stages are not configured: vertex shader, viewport
		 */
		public Pipeline build() {
			// Validate pipeline
			if(!shaders.containsKey(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)) throw new IllegalArgumentException("No vertex shader specified");
			if(pipeline.pViewportState == null) throw new IllegalArgumentException("No viewport stage specified");

//			// Create default layout if required
//			if(layout == null) {
//				layout = new Layout.Builder(dev).build();
//				// TODO - tracking
//			}

			// Init pipeline stages
			pipeline.stageCount = shaders.size();
			pipeline.pStages = StructureHelper.structures(shaders.values());

			// Init layout
//			pipeline.layout = layout.handle();

			// Init render pass
//			pipeline.renderPass = pass.handle();
			pipeline.subpass = 0;		// TODO

			// TODO
			pipeline.basePipelineHandle = null;
			pipeline.basePipelineIndex = -1;

			// Allocate pipeline
			final VulkanLibrary lib = dev.library();
			final Pointer[] pipelines = lib.factory().pointers(1);
			check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			return new Pipeline(pipelines[0], dev);
		}
	}
}
