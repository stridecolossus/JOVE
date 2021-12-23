package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.VkBufferMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkMemoryBarrier;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineCreateFlag;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ConstantsTable;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.StructureHelper;

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
				.append(handle)
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
			private ConstantsTable constants = new ConstantsTable();

			private ShaderStageBuilder(VkShaderStage stage) {
				this.stage = stage;
			}

			private ShaderStageBuilder(ShaderStageBuilder builder) {
				this.stage = builder.stage;
				this.shader = builder.shader;
				this.name = builder.name;
				this.constants = builder.constants;
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
			 * <p>
			 * Note that supported types are integer, float and boolean values.
			 * <p>
			 * @param constants Specialisation constants indexed by ID
			 * @throws IllegalArgumentException for a duplicate constant ID
			 * @throws IllegalArgumentException for an invalid or {@code null} constant
			 */
			public ShaderStageBuilder constants(ConstantsTable constants) {
				this.constants = notNull(constants);
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
				info.pSpecializationInfo = constants.build();
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

		// Derivatives
		private Handle baseHandle;
		private Builder base;
		private int baseIndex = -1;

		// Fixed function stages
		private final VertexInputPipelineStageBuilder input = new VertexInputPipelineStageBuilder();
		private final InputAssemblyPipelineStageBuilder assembly = new InputAssemblyPipelineStageBuilder();
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
		 * Helper - Sets this as a derivative pipeline.
		 * This is a synonym for {@link #flag(VkPipelineCreateFlag)} with a {@link VkPipelineCreateFlag#ALLOW_DERIVATIVES} flag.
		 * @see #derive(Pipeline)
		 */
		public Builder allowDerivatives() {
			return flag(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
		}

		/**
		 * Derives this pipeline from the given base pipeline.
		 * @param base Base pipeline to derive from
		 * @throws IllegalArgumentException if the given pipeline does not allow derivatives
		 * @see #allowDerivatives()
		 * @see #derive()
		 */
		public Builder derive(Pipeline base) {
			if(!base.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalArgumentException("Cannot derive from pipeline: " + base);
			this.baseHandle = base.handle();
			derive(this);
			return this;
		}

		/**
		 * Derives a new pipeline from this base pipeline.
		 * @return New builder for a derived pipeline
		 * @throws IllegalStateException if this pipeline does not allow derivatives
		 * @see #allowDerivatives()
		 * @see #derive(Pipeline)
		 */
		public Builder derive() {
			// Validate
			if(!flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalStateException("Cannot derive from this pipeline");

			// Create derived builder
			final Builder builder = new Builder();
			derive(builder);
			builder.base = this;

			// Clone pipeline properties
			builder.layout = layout;
			builder.pass = pass;
			builder.flags.addAll(flags);

			// Clone pipeline stages
			for(ShaderStageBuilder b : shaders.values()) {
				builder.shaders.put(b.stage, new ShaderStageBuilder(b));
			}
			builder.input.init(input);
			builder.assembly.init(assembly);
			builder.tesselation.init(tesselation);
			builder.viewport.init(viewport);
			builder.raster.init(raster);
			builder.multi.init(multi);
			builder.depth.init(depth);
			builder.blend.init(blend);
			builder.dynamic.init(dynamic);

			return builder;
		}

		/**
		 * Sets this pipeline as derived.
		 */
		private static void derive(Builder builder) {
			builder.flags.add(VkPipelineCreateFlag.DERIVATIVE);
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
		public InputAssemblyPipelineStageBuilder assembly() {
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
		 * Helper - Configures a single viewport and scissor with the given rectangle.
		 * @param rect Viewport/scissor rectangle
		 */
		public Builder viewport(Rectangle rect) {
			viewport.viewport(rect, true);
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
			if(shaders.containsKey(stage) && (base == null)) throw new IllegalArgumentException("Duplicate shader stage: " + stage);
			shaders.put(stage, shader);
			return shader;
		}

		/**
		 * Populates a pipeline descriptor.
		 */
		private void populate(VkGraphicsPipelineCreateInfo info) {
			// Init descriptor
			info.flags = IntegerEnumeration.mask(flags);

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
			info.pStages = StructureHelper.first(shaders.values(), VkPipelineShaderStageCreateInfo::new, ShaderStageBuilder::populate);

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

			// Init derivative pipeline
			if(base != null) {
				if(baseHandle != null) throw new IllegalArgumentException("Cannot specify a base pipeline and a derivative index");
				assert baseIndex >= 0;
			}
			info.basePipelineHandle = baseHandle;
			info.basePipelineIndex = baseIndex;
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
		 * @throws IllegalArgumentException if the pipeline layout or render pass have not been specified
		 * @throws IllegalArgumentException unless at least a {@link VkShaderStage#VERTEX} shader stage has been configured
		 */
		public static List<Pipeline> build(List<Builder> builders, PipelineCache cache, DeviceContext dev) {
			// Include base builders if not already present
			final List<Builder> list = new ArrayList<>(builders);
			builders
					.stream()
					.map(b -> b.base)
					.filter(Objects::nonNull)
					.filter(Predicate.not(list::contains))
					.forEach(list::add);

			// Init index for derived pipelines
			for(Builder b : list) {
				if(b.base != null) {
					b.baseIndex = list.indexOf(b.base);
					assert b.baseIndex >= 0;
				}
			}

			// Build array of descriptors
			final VkGraphicsPipelineCreateInfo[] array = StructureHelper.array(list, VkGraphicsPipelineCreateInfo::new, Builder::populate);

			// Allocate pipelines
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = new Pointer[array.length];
			check(lib.vkCreateGraphicsPipelines(dev, cache, array.length, array, null, handles));

			// Create pipelines
			return IntStream
					.range(0, array.length)
					.mapToObj(n -> create(handles[n], list.get(n), dev))
					.collect(toList());
		}

		/**
		 * Creates a pipeline.
		 */
		private static Pipeline create(Pointer handle, Builder builder, DeviceContext dev) {
			return new Pipeline(handle, dev, builder.layout, builder.flags);
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
		int vkCreateGraphicsPipelines(DeviceContext device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

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
