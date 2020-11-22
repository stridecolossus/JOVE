package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

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
		super(handle, dev, dev.library()::vkDestroyPipeline);
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, this.handle());
	}

	/**
	 * Builder for a pipeline.
	 */
	public static class Builder {
		// Properties
		private final LogicalDevice dev;
		private Layout layout;
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

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			init();
		}

		/**
		 * Initialises the nested builders.
		 */
		private void init() {
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
		 * @param layout Pipeline layout (default is {@link Pipeline.Layout#DEFAULT})
		 */
		public Builder layout(Layout layout) {
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
			shaders.init();
			return shaders;
		}

		/**
		 * Constructs this pipeline.
		 * @return New pipeline
		 * @throws IllegalArgumentException if any of the following stages are not configured: pipeline layout, vertex shader, viewport
		 */
		public Pipeline build() {
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
			pipeline.pStages = shaders.result();

			// Init fixed function pipeline stages
			pipeline.pVertexInputState = input.result();
			pipeline.pInputAssemblyState = assembly.result();
			pipeline.pViewportState = viewport.result();
			pipeline.pRasterizationState = raster.result();
			pipeline.pDepthStencilState = depth.result();
			pipeline.pColorBlendState = blend.result();
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
			final Pointer[] pipelines = lib.factory().pointers(1);
			check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			return new Pipeline(pipelines[0], dev);
		}
	}

	/**
	 * A <i>pipeline layout</i> specifies the resources used by a pipeline.
	 */
	public static class Layout extends AbstractVulkanObject {
		/**
		 * Constructor.
		 * @param handle		Pipeline handle
		 * @param dev			Logical device
		 */
		private Layout(Pointer handle, LogicalDevice dev) {
			super(handle, dev, dev.library()::vkDestroyPipelineLayout);
		}

		/**
		 * Builder for a pipeline layout.
		 */
		public static class Builder {
			private final LogicalDevice dev;
			private final List<DescriptorSet.Layout> sets = new ArrayList<>();
			// TODO - push constant layouts

			/**
			 * Constructor.
			 * @param dev			Logical device
			 * @param parent		Parent builder
			 * @param consumer		Consumer
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Adds a descriptor-set to this layout.
			 * @param layout Descriptor-set layout
			 */
			public Builder add(DescriptorSet.Layout layout) {
				Check.notNull(layout);
				sets.add(layout);
				return this;
			}

			/**
			 * Constructs this pipeline layout.
			 * @return New pipeline layout
			 */
			public Layout build() {
				// Init pipeline layout descriptor
				final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();

				// Add descriptor set layouts
				info.setLayoutCount = sets.size();
				info.pSetLayouts = Handle.toPointerArray(sets);

				// Add push constants
				// TODO

				// Allocate layout
				final VulkanLibrary lib = dev.library();
				final PointerByReference layout = lib.factory().pointer();
				check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));

				// Create layout
				return new Layout(layout.getValue(), dev);
			}
		}
	}
}
