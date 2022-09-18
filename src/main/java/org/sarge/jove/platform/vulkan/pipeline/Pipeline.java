package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.util.*;

import com.sun.jna.Pointer;

/**
 * A <i>pipeline</i> specifies the sequence of operations for graphics rendering.
 * @author Sarge
 */
public class Pipeline extends AbstractVulkanObject {
	private final PipelineLayout layout;
	private final Set<VkPipelineCreateFlag> flags;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 * @param layout		Pipeline layout
	 * @param flags			Pipeline flags
	 */
	Pipeline(Pointer handle, DeviceContext dev, PipelineLayout layout, Set<VkPipelineCreateFlag> flags) {
		super(handle, dev);
		this.layout = notNull(layout);
		this.flags = Set.copyOf(flags);
	}

	/**
	 * @return Layout of this pipeline
	 */
	public PipelineLayout layout() {
		return layout;
	}

	/**
	 * @return Pipeline flags
	 */
	public Set<VkPipelineCreateFlag> flags() {
		return flags;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(layout)
				.append(flags)
				.build();
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
			private VkSpecializationInfo constants;

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
			 * Adds specialisation constants to parameterise this shader.
			 * @param constants Specialisation constants or {@code null} to use default values
			 * @see Shader#constants(Map)
			 */
			public ShaderStageBuilder constants(VkSpecializationInfo constants) {
				this.constants = constants;
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
				info.sType = VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO;
				info.stage = stage;
				info.module = shader.handle();
				info.pName = name;
				info.pSpecializationInfo = constants;
			}

			private void validate() {
				if(shader == null) throw new IllegalArgumentException("Shader module not populated");
			}
		}

		// Properties
		private PipelineLayout layout;
		private RenderPass pass;
		private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
		private final Map<VkShaderStage, ShaderStageBuilder> shaders = new HashMap<>();

		// Derivative pipelines
		private Handle base;
		private Builder parent;

		// Fixed function stages
		private final VertexInputPipelineStageBuilder input = new VertexInputPipelineStageBuilder();
		private final AssemblyPipelineStageBuilder assembly = new AssemblyPipelineStageBuilder();
		private final TesselationPipelineStageBuilder tesselation = new TesselationPipelineStageBuilder();
		private final ViewportPipelineStageBuilder viewport = new ViewportPipelineStageBuilder();
		private final RasterizerPipelineStageBuilder raster = new RasterizerPipelineStageBuilder();
		private final MultiSamplePipelineStageBuilder multi = new MultiSamplePipelineStageBuilder();
		private final DepthStencilPipelineStageBuilder depth = new DepthStencilPipelineStageBuilder();
		private final ColourBlendPipelineStageBuilder blend = new ColourBlendPipelineStageBuilder();
		private final DynamicStatePipelineStageBuilder dynamic = new DynamicStatePipelineStageBuilder();

		/**
		 * Constructor.
		 */
		public Builder() {
			input.parent(this);
			assembly.parent(this);
			tesselation.parent(this);
			viewport.parent(this);
			raster.parent(this);
			multi.parent(this);
			depth.parent(this);
			blend.parent(this);
			dynamic.parent(this);
		}

		/**
		 * Sets the layout for this pipeline.
		 * @param layout Pipeline layout
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
		 * Sets a pipeline flag.
		 * @param flag Pipeline flag
		 * @throws IllegalArgumentException if the flag is {@link VkPipelineCreateFlag#DERIVATIVE} which cannot be used explicitly
		 */
		public Builder flag(VkPipelineCreateFlag flag) {
			if(flag == VkPipelineCreateFlag.DERIVATIVE) throw new IllegalArgumentException("Cannot explicitly set pipeline as a derivative, use derive()");
			this.flags.add(notNull(flag));
			return this;
		}

		/**
		 * Sets this as a parent from which pipelines can be derived.
		 * This is a synonym for {@link #flag(VkPipelineCreateFlag)} with a {@link VkPipelineCreateFlag#ALLOW_DERIVATIVES} flag.
		 * @see #derive(Pipeline)
		 * @see #derive(Builder)
		 */
		public Builder parent() {
			return flag(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
		}

		/**
		 * Derives this pipeline from an existing base pipeline.
		 * @param base Base pipeline
		 * @throws IllegalArgumentException if the parent pipeline does not allow derivatives or this pipeline is already derived
		 * @see #parent()
		 */
		public Builder derive(Pipeline base) {
			derive(base.flags());
			this.base = base.handle();
			return this;
		}

		/**
		 * Derives this pipeline from the given builder.
		 * @param parent Parent pipeline builder
		 * @throws IllegalArgumentException if the parent pipeline does not allow derivatives or this pipeline is already derived
		 * @throws IllegalStateException if the given parent is this builder
		 * @see #parent()
		 */
		public Builder derive(Builder parent) {
			if(parent == this) throw new IllegalStateException("Cannot derive from self");
			derive(parent.flags);
			this.parent = notNull(parent);
			return this;
		}

		/**
		 * @throws IllegalArgumentException if the parent pipeline does not allow derivatives or this pipeline is already derived
		 */
		private void derive(Set<VkPipelineCreateFlag> flags) {
			// Check pipeline can be derived
			if(!flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) {
				throw new IllegalArgumentException("Invalid parent pipeline");
			}

			// Mark as derivative
			if(this.flags.contains(VkPipelineCreateFlag.DERIVATIVE)) {
				throw new IllegalArgumentException("Pipeline is already a derivative");
			}
			this.flags.add(VkPipelineCreateFlag.DERIVATIVE);
		}

		/**
		 * @return Builder for the vertex input stage
		 */
		public VertexInputPipelineStageBuilder input() {
			return input;
		}

		/**
		 * @return Builder for the input assembly stage
		 */
		public AssemblyPipelineStageBuilder assembly() {
			return assembly;
		}

		/**
		 * @return Builder for the tesselation stage
		 */
		public TesselationPipelineStageBuilder tesselation() {
			return tesselation;
		}

		/**
		 * @return Builder for the viewport stage
		 */
		public ViewportPipelineStageBuilder viewport() {
			return viewport;
		}

		/**
		 * Convenience helper to add a viewport and scissor rectangle with the same dimensions.
		 * @param rect Viewport/scissor rectangle
		 */
		public Builder viewport(Rectangle rect) {
			viewport.viewport(rect);
			viewport.scissor(rect);
			return this;
		}

		/**
		 * @return Builder for the rasterizer stage
		 */
		public RasterizerPipelineStageBuilder rasterizer() {
			return raster;
		}

		/**
		 * @return Builder for the multi-sample stage
		 */
		public MultiSamplePipelineStageBuilder multi() {
			return multi;
		}

		/**
		 * @return Builder for the depth-stencil stage
		 */
		public DepthStencilPipelineStageBuilder depth() {
			return depth;
		}

		/**
		 * @return Builder for the colour-blend stage
		 */
		public ColourBlendPipelineStageBuilder blend() {
			return blend;
		}

		/**
		 * @return Builder for the dynamic state stage
		 */
		public DynamicStatePipelineStageBuilder dynamic() {
			return dynamic;
		}

		/**
		 * Starts a new programmable shader pipeline stage.
		 * @param stage Shader stage
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
		 * Convenience helper to add a shader stage with default configuration.
		 * @param stage		Shader stage
		 * @param shader	Shader module
		 */
		public Builder shader(VkShaderStage stage, Shader shader) {
			return shader(stage)
					.shader(shader)
					.build();
		}

		/**
		 * Populates the pipeline descriptor.
		 */
		private void populate(VkGraphicsPipelineCreateInfo info) {
			// Init descriptor
			info.flags = IntegerEnumeration.reduce(flags);

			// Init layout
			if(layout == null) throw new IllegalArgumentException("No pipeline layout specified");
			info.layout = layout.handle();

			// Init render pass
			if(pass == null) throw new IllegalArgumentException("No render pass specified");
			info.renderPass = pass.handle();
			info.subpass = 0;		// TODO - subpass?

			// Init shader pipeline stages
			if(!shaders.containsKey(VkShaderStage.VERTEX)) throw new IllegalStateException("No vertex shader specified");
			info.stageCount = shaders.size();
			info.pStages = StructureCollector.pointer(shaders.values(), new VkPipelineShaderStageCreateInfo(), ShaderStageBuilder::populate);

			// Init fixed function stages
			info.pVertexInputState = input.get();
			info.pInputAssemblyState = assembly.get();
			info.pTessellationState = tesselation.get();
			info.pViewportState = viewport.get();
			info.pRasterizationState = raster.get();
			info.pMultisampleState = multi.get();
			info.pDepthStencilState = depth.get();
			info.pColorBlendState = blend.get();			// TODO - check number of blend attachments = framebuffers
			info.pDynamicState = dynamic.get();

			// Init derivative pipelines
			info.basePipelineHandle = base;
			info.basePipelineIndex = -1;
		}

		/**
		 * Convenience variant to construct a single pipeline.
		 * @param cache		Optional pipeline cache
		 * @param dev		Logical device
		 * @return New pipeline
		 * @throws IllegalArgumentException if the pipeline layout or render pass have not been specified
		 * @throws IllegalArgumentException unless at least a {@link VkShaderStage#VERTEX} shader stage has been configured
		 */
		public Pipeline build(PipelineCache cache, DeviceContext dev) {
			final List<Pipeline> pipelines = build(List.of(this), cache, dev);
			return pipelines.get(0);
		}

		/**
		 * Constructs multiple pipelines.
		 * @param builders		Pipeline builders
		 * @param cache			Optional pipeline cache
		 * @param dev 			Logical device
		 * @return New pipelines
		 */
		public static List<Pipeline> build(List<Builder> builders, PipelineCache cache, DeviceContext dev) {
			// Build array of descriptors
			final VkGraphicsPipelineCreateInfo[] array = StructureCollector.array(builders, new VkGraphicsPipelineCreateInfo(), Builder::populate);

			// Patch derived pipeline indices
			for(int n = 0; n < array.length; ++n) {
				final Builder parent = builders.get(n).parent;
				if(parent == null) {
					continue;
				}
				final int index = builders.indexOf(parent);
				if(index == -1) throw new IllegalArgumentException("Parent pipeline not present in array: index=" + n);
				array[n].basePipelineIndex = index;
			}

			// Allocate pipelines
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = new Pointer[array.length];
			check(lib.vkCreateGraphicsPipelines(dev, cache, array.length, array, null, handles));

			// Create pipelines
			final Pipeline[] pipelines = new Pipeline[array.length];
			for(int n = 0; n < array.length; ++n) {
				final Builder builder = builders.get(n);
				pipelines[n] = new Pipeline(handles[n], dev, builder.layout, builder.flags);
			}
			return Arrays.asList(pipelines);
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
		 * @param pPipelines		Returned pipeline(s)
		 * @return Result
		 */
		int vkCreateGraphicsPipelines(DeviceContext device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

		/**
		 * Destroys a pipeline.
		 * @param device			Logical device
		 * @param pipeline			Pipeline
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipeline(DeviceContext device, Pipeline pipeline, Pointer pAllocator);

		/**
		 * Binds a pipeline to the render sequence.
		 * @param commandBuffer			Command buffer
		 * @param pipelineBindPoint		Bind-point
		 * @param pipeline				Pipeline to bind
		 */
		void vkCmdBindPipeline(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline);

		/**
		 * Command to apply a pipeline barrier.
		 * @param commandBuffer						Command buffer
		 * @param srcStageMask						Source pipeline stages mask
		 * @param dstStageMask						Destination pipeline stages mask
		 * @param dependencyFlags					Dependency mask
		 * @param memoryBarrierCount				Number of memory barriers
		 * @param pMemoryBarriers					Memory barriers
		 * @param bufferMemoryBarrierCount			Number of buffer barriers
		 * @param pBufferMemoryBarriers				Buffer barriers
		 * @param imageMemoryBarrierCount			Number of image barriers
		 * @param pImageMemoryBarriers				Image barriers
		 */
		void vkCmdPipelineBarrier(Buffer commandBuffer, int srcStageMask, int dstStageMask, int dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
	}
}
