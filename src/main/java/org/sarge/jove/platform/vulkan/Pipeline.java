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
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline</i> specifies the stages for rendering.
 * @author Sarge
 */
public class Pipeline extends VulkanHandle {
	/**
	 * Constructor.
	 * @param handle Pipeline handle
	 */
	Pipeline(VulkanHandle handle) {
		super(handle);
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
	 * <li>primitive assembly stage - assumes {@link Primitive#TRIANGLE}</li>
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
	 * <li>the pipeline will employ a default layout if none is provided using {@link #layout}</li>
	 * </ul>
	 * @see Layout.Builder
	 */
	public static class Builder {
		// fixed:
		// - primitive assembly
		// - rasterization
		// shader:
		// - tesselation
		// - multi-sample
		// - stencil

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
		 * Builder for the viewport stage descriptor.
		 */
		public class ViewportStageBuilder {
			private final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
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
				if(contains(info.stage)) throw new IllegalArgumentException("Duplicate shader stage: " + info.stage);
				info.verify();
				shaders.add(info);
				return Builder.this;
			}
		}

		/**
		 * Builder for a shader stage.
		 */
		public class ColourBlendStageBuilder {
			private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo(); // TODO - replace default
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
				// Add colour attachments
				assert !attachments.isEmpty();
				info.attachmentCount = attachments.size();
				info.pAttachments = StructureHelper.structures(attachments);

				// Attach colour blend stage
				pipeline.pColorBlendState = info;
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
		 * Starts the vertex input stage.
		 * @return New vertex input stage builder
		 */
		public VertexInputStageBuilder input() {
			return new VertexInputStageBuilder();
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
		 * Starts the viewport stage.
		 * @return New viewport stage builder
		 */
		public ViewportStageBuilder viewport() {
			return new ViewportStageBuilder();
		}

		/**
		 * Starts the colour blend stage.
		 * @return New colour blend stage builder
		 */
		public ColourBlendStageBuilder blend() {
			return new ColourBlendStageBuilder();
		}

		/**
		 * Sets the pipeline layout.
		 * @param layout Pipeline layout
		 */
		public Builder layout(Layout layout) {
			this.layout = notNull(layout);
			return this;
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
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final Pointer[] pipelines = vulkan.factory().pointers(1);
			check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

			// Create pipeline
			final Pointer handle = pipelines[0];
			final Destructor destructor = () -> lib.vkDestroyPipeline(dev.handle(), handle, null);
			return new Pipeline(new VulkanHandle(handle, destructor));
		}
	}

	/**
	 * Pipeline layout.
	 * TODO - just a handle?
	 */
	public static class Layout extends VulkanHandle {
		/**
		 * Constructor.
		 * @param handle Handle
		 */
		Layout(VulkanHandle handle) {
			super(handle);
		}

		/**
		 * Builder for a pipeline layout.
		 */
		public static class Builder {
			private final LogicalDevice dev;
			private final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();

			public Builder(LogicalDevice dev) {
				this.dev = notNull(dev);
			}

			// TODO
//			public int setLayoutCount;
//			public Pointer pSetLayouts;
//			public int pushConstantRangeCount;
//			public Pointer pPushConstantRanges;

			/**
			 * Constructs this layout.
			 * @param device Logical device
			 * @return New pipeline layout
			 */
			public Layout build() {
				// Create pipeline layout
				final Vulkan vulkan = Vulkan.instance();
				final VulkanLibrary lib = vulkan.library();
				final PointerByReference layout = vulkan.factory().reference();
				check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));

				// Create wrapper
				final Pointer handle = layout.getValue();
				final Destructor destructor = () -> lib.vkDestroyPipelineLayout(dev.handle(), handle, null);
				return new Layout(new VulkanHandle(handle, destructor));
			}
		}
	}
}
