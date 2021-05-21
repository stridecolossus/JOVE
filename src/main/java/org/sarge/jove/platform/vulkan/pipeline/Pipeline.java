package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;

/**
 * A <i>pipeline</i> specifies the sequence of operations for graphics rendering.
 * @author Sarge
 */
public class Pipeline extends AbstractVulkanObject {
	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 */
	Pipeline(Pointer handle, LogicalDevice dev) {
		super(handle, dev);
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, this.handle());
	}

	@Override
	protected Destructor destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipeline;
	}

	/**
	 * Builder for a pipeline.
	 */
	public static class Builder {
		// Properties
		private PipelineLayout layout;
		private RenderPass pass;
		private final ShaderStageBuilder shaders = new ShaderStageBuilder();

		// Fixed function builders
		private final VertexInputStageBuilder input = new VertexInputStageBuilder();
		private final InputAssemblyStageBuilder assembly = new InputAssemblyStageBuilder();
		// TODO - tessellation
		private final ViewportStageBuilder viewport = new ViewportStageBuilder();
		private final RasterizerStageBuilder raster = new RasterizerStageBuilder();
		private final DepthStencilStageBuilder depth = new DepthStencilStageBuilder();
		// TODO - multi sample
		private final ColourBlendStageBuilder blend = new ColourBlendStageBuilder();
		// TODO - dynamic

		public Builder() {
			// TODO - could use dynamic proxy for the parent but seems overkill?
			input.parent(this);
			assembly.parent(this);
			viewport.parent(this);
			raster.parent(this);
			depth.parent(this);
			blend.parent(this);
			shaders.parent(this);
		}

		/**
		 * Sets the layout for this pipeline.
		 * @param layout Pipeline layout (default is {@link PipelineLayout#IDENTITY})
		 */
		public Builder layout(PipelineLayout layout) {
			this.layout = notNull(layout);
			return this;
		}

		/**
		 * Sets the render pass for this pipeline.
		 * @param pass Render pass
		 */
		public Builder pass(RenderPass pass) {
			this.pass = notNull(pass);
			return this;
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
		 * Convenience method to create a flipped viewport and scissor with the given dimensions.
		 * @param size Viewport dimensions
		 */
		public Builder viewport(Dimensions size) {
			final Rectangle rect = new Rectangle(size);
			viewport.viewport(rect);
			viewport.scissor(rect);
			viewport.flip(true);
			return this;
		}

		/**
		 * @return Builder for the rasterizer stage
		 */
		public RasterizerStageBuilder rasterizer() {
			return raster;
		}

		/**
		 * @return Builder for the depth-stencil stage
		 */
		public DepthStencilStageBuilder depth() {
			return depth;
		}

		/**
		 * @return Builder for the colour-blend stage
		 */
		public ColourBlendStageBuilder blend() {
			return blend;
		}

		/**
		 * @return Builder for a shader stage
		 */
		public ShaderStageBuilder shader() {
			return shaders.init();
		}

		/**
		 * Constructs this pipeline.
		 * @param dev Logical device
		 * @return New pipeline
		 * @throws IllegalArgumentException if the pipeline layout or render pass has not been specified
		 */
		public Pipeline build(LogicalDevice dev) {
			// Create descriptor
			final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();

			// Init layout
			if(layout == null) throw new IllegalArgumentException("No pipeline layout specified");
			pipeline.layout = layout.handle();

			// Init render pass
			if(pass == null) throw new IllegalArgumentException("No render pass specified");
			pipeline.renderPass = pass.handle();
			pipeline.subpass = 0;		// TODO

			// Init shader pipeline stages
			pipeline.stageCount = shaders.size();
			pipeline.pStages = shaders.get();

			// Init fixed function pipeline stages
			pipeline.pVertexInputState = input.get();
			pipeline.pInputAssemblyState = assembly.get();
			pipeline.pViewportState = viewport.get();
			pipeline.pRasterizationState = raster.get();
			pipeline.pDepthStencilState = depth.get();
			pipeline.pColorBlendState = blend.get();
			// TODO - check number of blend attachments = framebuffers

			// TODO
			pipeline.pMultisampleState = new VkPipelineMultisampleStateCreateInfo();
			pipeline.pMultisampleState.sampleShadingEnable = VulkanBoolean.FALSE;
//			pipeline.pMultisampleState.rasterizationSamples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT.value();

			// TODO
			pipeline.basePipelineHandle = null;
			pipeline.basePipelineIndex = -1;

			// Allocate pipeline
			final VulkanLibrary lib = dev.library();
			final Pointer[] pipelines = lib.factory().array(1);
			check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			return new Pipeline(pipelines[0], dev);
		}
	}
}
