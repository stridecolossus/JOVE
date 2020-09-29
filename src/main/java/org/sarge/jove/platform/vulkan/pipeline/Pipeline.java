package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkGraphicsPipelineCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
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
	private final Pointer handle;
	private final LogicalDevice dev;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 */
	Pipeline(Pointer handle, LogicalDevice dev) {
		this.handle = notNull(handle);
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
		private final List<VkPipelineShaderStageCreateInfo> shaders = new ArrayList<>();

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			this.pipeline.pVertexInputState = new VertexInputStageBuilder<>().buildLocal();
		}

		/**
		 * @return Builder for the vertex input stage
		 */
		public VertexInputStageBuilder<Builder> input() {
			return new VertexInputStageBuilder<>() {
				@Override
				public Builder build() {
					pipeline.pVertexInputState = buildLocal();
					return Builder.this;
				}
			};
		}

		/**
		 * @return Builder for the viewport stage
		 */
		public ViewportStageBuilder<Builder> viewport() {
			return new ViewportStageBuilder<>() {
				@Override
				public Builder build() {
					pipeline.pViewportState = buildLocal();
					return Builder.this;
				}
			};
		}

		/**
		 * Constructs this pipeline.
		 * @return New pipeline
		 * @throws IllegalArgumentException if the vertex shader stage has not been configured
		 * @throws IllegalArgumentException if the viewport stage has not been configured
		 */
		public Pipeline build() {
//			// Validate pipeline
//			if(!contains(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)) throw new IllegalArgumentException("No vertex shader specified");
			if(pipeline.pViewportState == null) throw new IllegalArgumentException("No viewport stage specified");
//
//			// Create default layout if required
//			if(layout == null) {
//				layout = new Layout.Builder(dev).build();
//				// TODO - tracking
//			}

			// Init pipeline stages
			pipeline.stageCount = shaders.size();
			pipeline.pStages = StructureHelper.structures(shaders);

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
