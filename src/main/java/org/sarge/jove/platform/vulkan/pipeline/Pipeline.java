package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashMap;
import java.util.Map;

import org.sarge.jove.platform.vulkan.VkBufferMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;

/**
 * A <i>pipeline</i> specifies the sequence of operations for graphics rendering.
 * @author Sarge
 */
public class Pipeline extends AbstractVulkanObject {
	private final PipelineLayout layout;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 * @param layout		Pipeline layout
	 */
	Pipeline(Pointer handle, LogicalDevice dev, PipelineLayout layout) {
		super(handle, dev);
		this.layout = notNull(layout);
	}

	/**
	 * @return Layout of this pipeline
	 */
	public PipelineLayout layout() {
		return layout;
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, Pipeline.this);
	}

	@Override
	protected Destructor<Pipeline> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipeline;
	}

	/**
	 * Builder for a pipeline.
	 */
	public static class Builder {
		/**
		 * A <i>shader stage builder</i> configures a programmable shader stage for this pipeline.
		 */
		public class ShaderStageBuilder {
			private final VkShaderStage stage;
			private Shader shader;
			private String name = "main";

			private ShaderStageBuilder(VkShaderStage stage) {
				this.stage = stage;
			}

			/**
			 * Sets the shader module.
			 * @param shader Shader module
			 */
			public ShaderStageBuilder shader(Shader shader) {
				this.shader = notNull(shader);
				return this;
			}

			/**
			 * Sets the method name of this shader stage (default is {@code main}).
			 * @param name Shader method name
			 */
			public ShaderStageBuilder name(String name) {
				this.name = notEmpty(name);
				return this;
			}

			/**
			 * Constructs this shader pipeline stage.
			 * @return Parent pipeline builder
			 * @throws IllegalArgumentException if the shader module has not been configured
			 */
			public Builder build() {
				validate();
				return Builder.this;
			}

			/**
			 * Populates the shader stage descriptor.
			 */
			void populate(VkPipelineShaderStageCreateInfo info) {
				validate();
				info.stage = stage;
				info.module = shader.handle();
				info.pName = name;
			}

			private void validate() {
				if(shader == null) throw new IllegalArgumentException("Shader module not populated");
			}
		}

		// Properties
		private PipelineLayout layout;
		private PipelineCache cache;
		private RenderPass pass;
		private final Map<VkShaderStage, ShaderStageBuilder> shaders = new HashMap<>();

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
			input.parent(this);
			assembly.parent(this);
			viewport.parent(this);
			raster.parent(this);
			depth.parent(this);
			blend.parent(this);
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
		 * Sets the pipeline cache.
		 * @param cache Pipeline cache
		 */
		public Builder cache(PipelineCache cache) {
			this.cache = notNull(cache);
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
		 * @param stage Programmable shader stage
		 * @return Builder for a shader stage
		 * @throws IllegalArgumentException for a duplicate shader stage
		 */
		public ShaderStageBuilder shader(VkShaderStage stage) {
			final var shader = new ShaderStageBuilder(stage);
			if(shaders.containsKey(stage)) throw new IllegalArgumentException("Duplicate shader stage: " + stage);
			shaders.put(stage, shader);
			return shader;
		}

		/**
		 * Constructs this pipeline.
		 * @param dev Logical device
		 * @return New pipeline
		 * @throws IllegalArgumentException if the pipeline layout or render pass has not been specified
		 * @throws IllegalArgumentException unless at least a {@link VkShaderStage#VERTEX} shader has been specified
		 */
		public Pipeline build(LogicalDevice dev) {
			// Create descriptor
			final var pipeline = new VkGraphicsPipelineCreateInfo();

			// Init layout
			if(layout == null) throw new IllegalArgumentException("No pipeline layout specified");
			pipeline.layout = layout.handle();

			// Init render pass
			if(pass == null) throw new IllegalArgumentException("No render pass specified");
			pipeline.renderPass = pass.handle();
			pipeline.subpass = 0;		// TODO - subpass?

			// Init shader pipeline stages
			if(!shaders.containsKey(VkShaderStage.VERTEX)) throw new IllegalStateException("No vertex shader specified");
			pipeline.stageCount = shaders.size();
			pipeline.pStages = StructureHelper.first(shaders.values(), VkPipelineShaderStageCreateInfo::new, ShaderStageBuilder::populate);

			// Init fixed function pipeline stages
			pipeline.pVertexInputState = input.get();
			pipeline.pInputAssemblyState = assembly.get();
			pipeline.pViewportState = viewport.get();
			pipeline.pRasterizationState = raster.get();
			pipeline.pDepthStencilState = depth.get();
			pipeline.pColorBlendState = blend.get();
			// TODO - check number of blend attachments = framebuffers

			// TODO - multi-sampling
			pipeline.pMultisampleState = new VkPipelineMultisampleStateCreateInfo();
			pipeline.pMultisampleState.sampleShadingEnable = VulkanBoolean.FALSE;
//			pipeline.pMultisampleState.rasterizationSamples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT.value();

			// TODO - derive from pipeline (faster to create, faster to bind if same parent)
			pipeline.basePipelineHandle = null;			// TODO - from existing pipeline
			pipeline.basePipelineIndex = -1;			// TODO - or from pipeline in this call

			// Allocate pipeline
			final VulkanLibrary lib = dev.library();
			final Pointer[] pipelines = new Pointer[1];
			check(lib.vkCreateGraphicsPipelines(dev, cache, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			return new Pipeline(pipelines[0], dev, layout);
		}
	}

	/**
	 * Pipeline API.
	 */
	public interface Library {
		/**
		 * Creates a graphics pipeline.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline handle(s)
		 * @return Result code
		 */
		int vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

		/**
		 * Destroys a pipeline.
		 * @param device			Logical device
		 * @param pipeline			Pipeline
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipeline(DeviceContext device, Pipeline pipeline, Pointer pAllocator);

		/**
		 * Command to bind a pipeline.
		 * @param commandBuffer			Command buffer
		 * @param pipelineBindPoint		Bind-point
		 * @param pipeline				Pipeline to bind
		 */
		void vkCmdBindPipeline(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline);

		/**
		 * Command to apply a pipeline barrier.
		 * @param commandBuffer
		 * @param srcStageMask
		 * @param dstStageMask
		 * @param dependencyFlags
		 * @param memoryBarrierCount
		 * @param pMemoryBarriers
		 * @param bufferMemoryBarrierCount
		 * @param pBufferMemoryBarriers
		 * @param imageMemoryBarrierCount
		 * @param pImageMemoryBarriers
		 */
		void vkCmdPipelineBarrier(Buffer commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
	}
}
