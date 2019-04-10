package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline</i> specifies the stages for rendering.
 * @author Sarge
 */
public class Pipeline extends LogicalDeviceHandle {
	private final Pipeline.Layout layout;

	/**
	 * Constructor.
	 * @param handle 		Pipeline handle
	 * @param layout		Pipeline layout
	 */
	Pipeline(Pointer handle, LogicalDevice dev, Pipeline.Layout layout) {
		super(handle, dev, lib -> lib::vkDestroyPipeline);
		this.layout = notNull(layout);
	}

	/**
	 * @return Layout of this pipeline
	 */
	public Pipeline.Layout layout() {
		return layout;
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
	 * <p>
	 * The following stages are initialised to suitable defaults:
	 * <ul>
	 * <li>vertex input stage - defaults to an empty state, i.e. not vertices</li>
	 * <li>primitive assembly stage - assumes {@link Primitive#TRIANGLE_LIST}</li>
	 * <li>rasterization stage</li>
	 * </ul>
	 * The following stages are mandatory:
	 * <ul>
	 * <li>vertex shader stage</li>
	 * <li>viewport stage</li>
	 * </ul>
	 * All other stages are optional.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>a default pipeline layout will be created if none is provided using {@link #layout}</li>
	 * </ul>
	 * @see Layout.Builder
	 */
	public static class Builder {
		/**
		 * Builder for the vertex input stage descriptor.
		 */
		public class VertexInputStageBuilder {
			private final List<VkVertexInputBindingDescription> bindings = new ArrayList<>();
			private final List<VkVertexInputAttributeDescription> attributes = new ArrayList<>();

			private VertexInputStageBuilder() {
			}

			/**
			 * Adds a binding description.
			 * @param layout Buffer layout
			 * @throws IllegalArgumentException for a duplicate binding index
			 * @throws IllegalArgumentException for a duplicate attribute location index
			 */
			public VertexInputStageBuilder binding(DataBuffer.Layout layout) {
				// Add binding descriptor
				if(bindings.stream().anyMatch(d -> d.binding == layout.binding())) throw new IllegalArgumentException("Duplicate binding index: " + layout.binding());
				final VkVertexInputBindingDescription binding = new VkVertexInputBindingDescription();
				binding.binding = layout.binding();
				binding.stride = layout.stride();
				binding.inputRate = rate(layout);
				bindings.add(binding);

				// Add attribute descriptors
				for(DataBuffer.Layout.Attribute attribute : layout.attributes()) {
					if(attributes.stream().anyMatch(attr -> attr.location == attribute.location())) throw new IllegalArgumentException("Duplicate attribute location: " + attribute.location());
					final VkVertexInputAttributeDescription attr = new VkVertexInputAttributeDescription();
					attr.binding = binding.binding;
					attr.location = attribute.location();
					attr.format = VulkanHelper.format(attribute.component());
					attr.offset = attribute.offset();
					attributes.add(attr);
				}

				return this;
			}

			private VkVertexInputRate rate(DataBuffer.Layout layout) {
				switch(layout.rate()) {
				case VERTEX:		return VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX;
				case INSTANCE:		return VkVertexInputRate.VK_VERTEX_INPUT_RATE_INSTANCE;
				default:			throw new UnsupportedOperationException("Unsupported input rate: " + layout.rate());
				}
			}

			/**
			 * Constructs this vertex input stage.
			 * @return New vertex input stage
			 * @throws IllegalArgumentException if no bindings were specified
			 */
			public Builder build() {
				// Init descriptor
				if(bindings.isEmpty()) throw new IllegalArgumentException("Bindings cannot be empty");
				final VkPipelineVertexInputStateCreateInfo info = new VkPipelineVertexInputStateCreateInfo();

				// Add binding descriptions
				info.vertexBindingDescriptionCount = bindings.size();
				info.pVertexBindingDescriptions = StructureHelper.structures(bindings);

				// Add attributes
				info.vertexAttributeDescriptionCount = attributes.size();
				info.pVertexAttributeDescriptions = StructureHelper.structures(attributes);

				// Set vertex input stage descriptor
				pipeline.pVertexInputState = info;
				return Builder.this;
			}
		}

		/**
		 * Builder for the rasterization stage.
		 */
		public class RasterizationStageBuilder {
			private RasterizationStageBuilder() {
				// TODO - init here?
			}

			/**
			 * Sets the polygon fill mode.
			 * @param mode Polygon mode
			 */
			public RasterizationStageBuilder mode(VkPolygonMode mode) {
				pipeline.pRasterizationState.polygonMode = notNull(mode);
				return this;
			}

			/**
			 * Sets the face culling mode.
			 * @param mode culling mode
			 */
			public RasterizationStageBuilder cull(VkCullModeFlag mode) {
				pipeline.pRasterizationState.cullMode = notNull(mode);
				return this;
			}

			/**
			 * Sets the front-face winding order.
			 * @param front Front face winding order
			 */
			public RasterizationStageBuilder front(VkFrontFace front) {
				pipeline.pRasterizationState.frontFace = notNull(front);
				return this;
			}

			/**
			 * Constructs this rasterization stage.
			 * @return Parent builder
			 */
			public Builder build() {
				return Builder.this;
			}
		}

		/**
		 * Builder for the viewport stage descriptor.
		 */
		public class ViewportStageBuilder {
			private final Deque<VkViewport> viewports = new ArrayDeque<>();
			private final List<VkRect2D> scissors = new ArrayList<>();

			private ViewportStageBuilder() {
			}

			/**
			 * Adds a viewport.
			 * @param rect 		Viewport rectangle
			 * @param min		Minimum depth
			 * @param max		Maximum depth
			 */
			public ViewportStageBuilder viewport(Rectangle rect, float min, float max) {
				final ScreenCoordinate coords = rect.position();
				final Dimensions dim = rect.dimensions();
				final VkViewport viewport = new VkViewport();
				viewport.x = coords.x;
				viewport.y = coords.y;
				viewport.width = dim.width;
				viewport.height = dim.height;
				viewport.minDepth = min;
				viewport.maxDepth = max;
				viewports.add(viewport);
				return this;
			}

			/**
			 * Adds a viewport with default min/max depth.
			 * @param rect Viewport rectangle
			 */
			public ViewportStageBuilder viewport(Rectangle rect) {
				return viewport(rect, 0, 1);
			}

			/**
			 * Adds a scissor rectangle.
			 * @param rect Scissor rectangle
			 */
			public ViewportStageBuilder scissor(Rectangle rect) {
				scissors.add(new VkRect2D(rect));
				return this;
			}

			// TODO
			// - what does multiple viewports actually do?
			// - have to have same number of scissors as viewports? or none?
			// - add auto method for scissor = last viewport?

			/**
			 * Constructs the viewport stage.
			 * @return Parent builder
			 */
			public Builder build() {
				// Add viewports
				final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
				if(viewports.isEmpty()) throw new IllegalArgumentException("No viewports specified");
				info.pViewports = StructureHelper.structures(viewports);
				info.viewportCount = viewports.size();

				// Add scissors
				if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
				info.pScissors = StructureHelper.structures(scissors);
				info.scissorCount = scissors.size();

				// Add viewport stage
				pipeline.pViewportState = info;
				return Builder.this;
			}
		}

		/**
		 * Builder for the depth-stencil stage.
		 */
		public class DepthStencilStageBuilder {
			/**
			 * Constructor.
			 */
			private DepthStencilStageBuilder() {
				pipeline.pDepthStencilState = new VkPipelineDepthStencilStateCreateInfo();
				enable(true);
				write(true);
				operation(VkCompareOp.VK_COMPARE_OP_LESS);
				// TODO
//				pipeline.pDepthStencilState.depthBoundsTestEnable = VulkanBoolean.FALSE;
//				pipeline.pDepthStencilState.stencilTestEnable = VulkanBoolean.FALSE;
//				pipeline.pDepthStencilState.front = new VkStencilOpState();
//				pipeline.pDepthStencilState.front.failOp = VkStencilOp.VK_STENCIL_OP_REPLACE;
//				pipeline.pDepthStencilState.back = new VkStencilOpState();
//				pipeline.pDepthStencilState.minDepthBounds = 0;
//				pipeline.pDepthStencilState.maxDepthBounds = 1;
			}

			/**
			 * Sets whether depth-testing is enabled.
			 * @param depth Whether depth-test is enabled
			 */
			public DepthStencilStageBuilder enable(boolean depth) {
				pipeline.pDepthStencilState.depthTestEnable = VulkanBoolean.of(depth);
				return this;
			}

			/**
			 * Sets whether depth-test writing is enabled.
			 * @param depth Whether new depths are written to the buffer
			 */
			public DepthStencilStageBuilder write(boolean write) {
				pipeline.pDepthStencilState.depthWriteEnable = VulkanBoolean.of(write);
				return this;
			}

			/**
			 * Sets the comparison operation for the depth test.
			 * @param op Comparison operator
			 */
			public DepthStencilStageBuilder operation(VkCompareOp op) {
				pipeline.pDepthStencilState.depthCompareOp = notNull(op);
				return this;
			}

			// TODO - depthBoundsTestEnable, min, max
			// TODO - stencil, front, back

			/**
			 * Constructs this colour blend stage.
			 * @return Parent builder
			 */
			public Builder build() {
				return Builder.this;
			}
		}

		/**
		 * Builder for a shader stage.
		 */
		public class ColourBlendStageBuilder {
			private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
			private final List<VkPipelineColorBlendAttachmentState> attachments = new ArrayList<>();

			private VkPipelineColorBlendAttachmentState current;

			private ColourBlendStageBuilder() {
				attachment();
			}

			/**
			 * Starts a colour blend attachment.
			 * @param stage Stage
			 */
			public ColourBlendStageBuilder attachment() {
				current = new VkPipelineColorBlendAttachmentState();
				attachments.add(current);
				return this;
			}

			// TODO - factor out attachment builder?
			// TODO - attachment fields
			// TODO - other info fields

			/**
			 * Constructs this colour blend stage.
			 * @return Parent builder
			 */
			public Builder build() {
				assert !attachments.isEmpty();
				info.attachmentCount = attachments.size();
				info.pAttachments = StructureHelper.structures(attachments);
				pipeline.pColorBlendState = info;
				return Builder.this;
			}
		}

		/**
		 * Builder for a shader stage.
		 */
		public class ShaderStageBuilder {
			private final VkPipelineShaderStageCreateInfo info = new VkPipelineShaderStageCreateInfo();

			private ShaderStageBuilder() {
			}

			/**
			 * Sets the stage.
			 * @param stage Stage
			 */
			public ShaderStageBuilder stage(VkShaderStageFlag stage) {
				info.stage = notNull(stage);
				return this;
			}

			/**
			 * Sets the shader module for this stage.
			 * @param shader Shader module
			 */
			public ShaderStageBuilder module(VulkanShader shader) {
				info.module = shader.handle();
				return this;
			}

			/**
			 * Sets the shader entry-point name for this stage.
			 * @param name Entry-point name
			 */
			public ShaderStageBuilder name(String name) {
				info.pName = notEmpty(name);
				return this;
			}

			// TODO - specialization

			/**
			 * Constructs this shader stage.
			 * @return Parent builder
			 * @throws IllegalArgumentException if the shader stage is not complete
			 * @throws IllegalArgumentException for a duplicate shader stage
			 */
			public Builder build() {
				Check.notNull(info.stage);
				Check.notNull(info.module);
				if(contains(info.stage)) throw new IllegalArgumentException("Duplicate shader stage: " + info.stage);
				shaders.add(info);
				return Builder.this;
			}
		}

		private final LogicalDevice dev;
		private final RenderPass pass;
		private final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();
		private final List<VkPipelineShaderStageCreateInfo> shaders = new ArrayList<>();
		private Layout layout;
		// TODO - dynamic state

		/**
		 * Constructor.
		 * @param dev		Device
		 * @param pass		Render pass
		 */
		public Builder(LogicalDevice dev, RenderPass pass) {
			this.dev = notNull(dev);
			this.pass = notNull(pass);
			init();
		}

		/**
		 * Initialises default pipeline stages.
		 */
		private void init() {
			pipeline.pVertexInputState = new VkPipelineVertexInputStateCreateInfo();
			pipeline.pInputAssemblyState = new VkPipelineInputAssemblyStateCreateInfo();
			pipeline.pRasterizationState = new VkPipelineRasterizationStateCreateInfo();
			pipeline.pMultisampleState = new VkPipelineMultisampleStateCreateInfo();
			new ColourBlendStageBuilder().build();
		}

		/**
		 * Sets the pipeline layout.
		 * @param layout Pipeline layout
		 */
		public Builder layout(Layout layout) {
			this.layout = notNull(layout);
			return this;
		}
		// TODO - should return layout builder?

		/**
		 * Starts the vertex input stage.
		 * @return New vertex input stage builder
		 */
		public VertexInputStageBuilder input() {
			return new VertexInputStageBuilder();
		}

		/**
		 * Starts the depth-stencil stage.
		 * @return New depth-stencil builder
		 */
		public DepthStencilStageBuilder depthStencil() {
			return new DepthStencilStageBuilder();
		}

		/**
		 * Sets the primitive topology.
		 * @param primitive Primitive
		 */
		public Builder primitive(Primitive primitive) {
			pipeline.pInputAssemblyState.topology = VulkanHelper.topology(primitive);
			return this;
		}

		/**
		 * Sets whether primitive restart is enabled.
		 * @param restart Whether restart is enabled
		 */
		public Builder restart(boolean restart) {
			pipeline.pInputAssemblyState.primitiveRestartEnable = VulkanBoolean.of(restart);
			return this;
		}

		/**
		 * Starts the viewport stage.
		 * @return New viewport stage builder
		 */
		public ViewportStageBuilder viewport() {
			return new ViewportStageBuilder();
		}

		/**
		 * Starts the rasterization stage.
		 * @return New rasterization stage builder
		 */
		public RasterizationStageBuilder rasterization() {
			return new RasterizationStageBuilder();
		}

		/**
		 * Starts the colour blend stage.
		 * @return New colour blend stage builder
		 */
		public ColourBlendStageBuilder blend() {
			return new ColourBlendStageBuilder();
		}

		/**
		 * Starts a shader stage.
		 * @return New shader stage builder
		 */
		public ShaderStageBuilder shader() {
			return new ShaderStageBuilder();
		}

		/**
		 * @param stage Shader stage
		 * @return Whether the given shader stage is present
		 */
		private boolean contains(VkShaderStageFlag stage) {
			return shaders.stream().anyMatch(s -> s.stage == stage);
		}

		/**
		 * Constructs this pipeline.
		 * @param ctx 		Device context
		 * @param pass		Render pass
		 * @return New pipeline
		 * @throws IllegalArgumentException if the pipeline is incomplete
		 * @throws ServiceException if the pipeline cannot be created
		 */
		public Pipeline build() {
			// Validate pipeline
			if(!contains(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)) throw new IllegalArgumentException("No vertex shader specified");
			if(pipeline.pViewportState == null) throw new IllegalArgumentException("No viewport stage specified");

			// Create default layout if required
			if(layout == null) {
				layout = new Layout.Builder(dev).build();
				// TODO - tracking
			}

			// Init descriptor
			pipeline.stageCount = shaders.size();
			pipeline.pStages = StructureHelper.structures(shaders);
			pipeline.layout = layout.handle();
			pipeline.renderPass = pass.handle();
			pipeline.subpass = 0;		// TODO
			pipeline.basePipelineHandle = null;
			pipeline.basePipelineIndex = -1;

			// Allocate pipeline
			final Vulkan vulkan = dev.vulkan();
			final VulkanLibrary lib = vulkan.library();
			final Pointer[] pipelines = vulkan.factory().pointers(1);
			check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			return new Pipeline(pipelines[0], dev, layout);
		}
	}

	/**
	 * Pipeline layout.
	 * TODO - just a handle?
	 */
	public static class Layout extends LogicalDeviceHandle {
		/**
		 * Constructor.
		 * @param handle Handle
		 */
		Layout(Pointer handle, LogicalDevice dev) {
			super(handle, dev, lib -> lib::vkDestroyPipelineLayout);
		}

		/**
		 * Builder for a pipeline layout.
		 */
		public static class Builder {
			private final LogicalDevice dev;
			private final List<Pointer> sets = new StrictList<>();
			// TODO - push constant layouts

			/**
			 * Constructor.
			 * @param dev Logical device
			 */
			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			/**
			 * Adds a descriptor set layout.
			 * @param layout Descriptor set layout
			 */
			public Builder add(DescriptorSet.Layout layout) {
				// TODO - check for duplicates?
				sets.add(layout.handle());
				return this;
			}

			// TODO - push constants

			/**
			 * Constructs this layout.
			 * @param device Logical device
			 * @return New pipeline layout
			 */
			public Layout build() {
				// Validate
				// TODO - error if both empty

				// Init pipeline layout descriptor
				final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();

				// Add descriptor set layouts
				info.setLayoutCount = sets.size();
				info.pSetLayouts = StructureHelper.pointers(sets);

				// Add push constants
				// TODO

				// Allocate layout
				final Vulkan vulkan = dev.vulkan();
				final VulkanLibrary lib = vulkan.library();
				final PointerByReference layout = vulkan.factory().reference();
				check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));

				// Create layout
				return new Layout(layout.getValue(), dev);
			}
		}
	}
}
