package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
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
		// Properties
		private final LogicalDevice dev;
		private PipelineLayout.Builder layout;
//		private RenderPass pass;
		private final Map<VkShaderStageFlag, VkPipelineShaderStageCreateInfo> shaders = new HashMap<>();

		// Fixed function builders
		private final VertexInputStageBuilder input = new VertexInputStageBuilder();
		private final InputAssemblyStageBuilder assembly = new InputAssemblyStageBuilder();
		// TODO - tessellation
		private final ViewportStageBuilder viewport = new ViewportStageBuilder();
		private final RasterizerStageBuilder raster = new RasterizerStageBuilder();
		// TODO - multi sample
		// TODO - depth stencil
		private final ColourBlendStageBuilder blend = new ColourBlendStageBuilder();
		// TODO - dynamic

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			this.layout = new PipelineLayout.Builder(dev);
			init();
		}

		/**
		 * Initialises the nested builders.
		 */
		private void init() {
			layout.parent(this);
			input.parent(this);
			assembly.parent(this);
			viewport.parent(this);
			raster.parent(this);
			blend.parent(this);
		}

		/**
		 * @return Builder for the pipeline layout
		 */
		public PipelineLayout.Builder layout() {
			return layout;
		}

		/**
		 * @return Builder for the vertex input stage
		 */
		public VertexInputStageBuilder input() {
			return input;
		}

		/**
		 * @return Builder for the input assembly stage
		 */
		public InputAssemblyStageBuilder assembly() {
			return assembly;
		}

		/**
		 * @return Builder for the viewport stage
		 */
		public ViewportStageBuilder viewport() {
			return viewport;
		}

		/**
		 * @return Builder for the rasterizer stage
		 */
		public RasterizerStageBuilder rasterizer() {
			return raster;
		}

		/**
		 * @return Builder for the colour-blend stage
		 */
		public ColourBlendStageBuilder blend() {
			return blend;
		}

		/**
		 * @return Builder for a shader stage
		 * @throws IllegalArgumentException for a duplicate stage
		 */
		public ShaderStageBuilder shader() {
			return new ShaderStageBuilder() {
				@Override
				public Builder build() {
					final VkPipelineShaderStageCreateInfo info = super.result();
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
			if(viewport == null) throw new IllegalArgumentException("No viewport stage specified");

			// Create descriptor
			final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();

			// Init layout
			pipeline.layout = layout.result().handle();

			// Init render pass
//			pipeline.renderPass = pass.handle();
			pipeline.subpass = 0;		// TODO

			// Init shader pipeline stages
			pipeline.stageCount = shaders.size();
			pipeline.pStages = StructureHelper.structures(shaders.values());

			// Init fixed function pipeline stages
			pipeline.pVertexInputState = input.result();
			pipeline.pInputAssemblyState = assembly.result();
			pipeline.pViewportState = viewport.result();
			pipeline.pRasterizationState = raster.result();
			pipeline.pColorBlendState = blend.result();

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
