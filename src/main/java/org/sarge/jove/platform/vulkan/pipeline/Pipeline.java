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
import java.util.Set;
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
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
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
	Pipeline(Pointer handle, LogicalDevice dev, PipelineLayout layout, Set<VkPipelineCreateFlag> flags) {
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
			private final ConstantsTable constants = new ConstantsTable();

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
			 * <p>
			 * Note that supported types are scalar (integer, float) and boolean values.
			 * <p>
			 * @param constants Specialisation constants indexed by ID
			 * @throws IllegalArgumentException for a duplicate constant ID
			 * @throws IllegalArgumentException for an invalid or {@code null} constant
			 */
			public ShaderStageBuilder constants(Map<Integer, Object> constants) {
				this.constants.add(constants);
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

		/**
		 *
		 */
		private static class Entry {
			private PipelineLayout layout;
			private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
			private final VkGraphicsPipelineCreateInfo info = new VkGraphicsPipelineCreateInfo();

			private Pipeline create(Pointer handle, LogicalDevice dev) {
				return new Pipeline(handle, dev, layout, flags);
			}
		}

		// Entries
		private final List<Entry> entries = new ArrayList<>();
		private Entry entry;

		// Properties
		private PipelineCache cache;
		private RenderPass pass;
		private final Map<VkShaderStage, ShaderStageBuilder> shaders = new HashMap<>();

		// Derivative
		private Handle base;
		private int baseIndex = -1;

		// Fixed function stages
		private final VertexInputPipelineStageBuilder input = new VertexInputPipelineStageBuilder(this);
		private final InputAssemblyPipelineStageBuilder assembly = new InputAssemblyPipelineStageBuilder(this);
		private final TesselationPipelineStageBuilder tesselation = new TesselationPipelineStageBuilder(this);
		private final ViewportPipelineStageBuilder viewport = new ViewportPipelineStageBuilder(this);
		private final RasterizerPipelineStageBuilder raster = new RasterizerPipelineStageBuilder(this);
		private final MultiSamplePipelineStageBuilder multi = new MultiSamplePipelineStageBuilder(this);
		private final DepthStencilPipelineStageBuilder depth = new DepthStencilPipelineStageBuilder(this);
		private final ColourBlendPipelineStageBuilder blend = new ColourBlendPipelineStageBuilder(this);
		private final DynamicStatePipelineStageBuilder dynamic = new DynamicStatePipelineStageBuilder(this);

		/**
		 * Constructor.
		 */
		public Builder() {
			init();
		}

		/**
		 * Starts a new pipeline entry.
		 */
		private void init() {
			entry = new Entry();
			entries.add(entry);
		}

		/**
		 * Sets the layout for this pipeline.
		 * @param layout Pipeline layout (default is {@link PipelineLayout#IDENTITY})
		 */
		public Builder layout(PipelineLayout layout) {
			entry.layout = notNull(layout);
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
		 * Sets a pipeline flag.
		 * @param flag Pipeline flag
		 */
		public Builder flag(VkPipelineCreateFlag flag) {
			entry.flags.add(notNull(flag));
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
		 * @see #allowDerivatives()
		 */
		public Builder derive(Pipeline base) {
			if(!base.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalArgumentException("Cannot derive from pipeline: " + base);
			this.base = base.handle();
			flag(VkPipelineCreateFlag.DERIVATIVE);
			return this;
		}

		/**
		 * Derives this pipeline from the most recent pipeline in this group that allows derivatives.
		 * @see #allowDerivatives()
		 */
		public Builder derive() {
			//this.baseIndex = zeroOrMore(index);
			// TODO - validate index and is allowed
			flag(VkPipelineCreateFlag.DERIVATIVE);
			return this;
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
			if(shaders.containsKey(stage)) throw new IllegalArgumentException("Duplicate shader stage: " + stage);
			shaders.put(stage, shader);
			return shader;
		}

		/**
		 * Begins a new pipeline.
		 */
		public Builder begin() {
			complete();
			init();
			shaders.clear();
			return this;
		}

		/**
		 * Completes the current pipeline entry.
		 */
		private void complete() {
			// Init descriptor
			final VkGraphicsPipelineCreateInfo info = entry.info;
			info.flags = IntegerEnumeration.mask(entry.flags);

			// Init layout
			if(entry.layout == null) throw new IllegalArgumentException("No pipeline layout specified");
			info.layout = entry.layout.handle();

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
			if((base != null) && (baseIndex >= 0)) throw new IllegalArgumentException("Cannot specify a base pipeline and a derivative index");
			info.basePipelineHandle = base;
			info.basePipelineIndex = baseIndex;
		}

		/**
		 * Constructs the pipeline(s).
		 * @param dev Logical device
		 * @return New pipeline(s)
		 * @throws IllegalArgumentException if a pipeline layout or render pass have not been specified
		 * @throws IllegalArgumentException unless at least a {@link VkShaderStage#VERTEX} shader stage has been configured
		 */
		public List<Pipeline> buildAll(LogicalDevice dev) {
			// Complete last pipeline
			complete();

			// Convert descriptors to array
			final int num = entries.size();
			final VkGraphicsPipelineCreateInfo[] array = entries
					.stream()
					.map(e -> e.info)
					.toArray(VkGraphicsPipelineCreateInfo[]::new);

			// Allocate pipelines
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = new Pointer[num];
			check(lib.vkCreateGraphicsPipelines(dev, cache, num, array, null, handles));

			// Create pipelines
			return IntStream
					.range(0, num)
					.mapToObj(n -> entries.get(n).create(handles[n], dev))
					.collect(toList());
		}

		/**
		 *
		 * @param dev
		 * @return
		 */
		public Pipeline build(LogicalDevice dev) {
			if(entries.size() != 1) throw new IllegalArgumentException("Expected exactly one pipeline");
			final List<Pipeline> pipelines = buildAll(dev);
			return pipelines.get(0);
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
