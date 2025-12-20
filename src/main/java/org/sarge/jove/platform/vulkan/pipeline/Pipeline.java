package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Updated;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>pipeline</i> defines the configuration for graphics rendering or compute shaders.
 * @author Sarge
 */
public class Pipeline extends VulkanObject {
	private final VkPipelineBindPoint type;
	private final PipelineLayout layout;
	private final boolean parent;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param device		Logical device
	 * @param type			Type of pipeline
	 * @param layout		Pipeline layout
	 * @param parent		Whether this is a parent pipeline
	 */
	Pipeline(Handle handle, LogicalDevice device, VkPipelineBindPoint type, PipelineLayout layout, boolean parent) {
		super(handle, device);
		this.type = requireNonNull(type);
		this.layout = requireNonNull(layout);
		this.parent = parent;
	}

	/**
	 * @return Type of this pipeline
	 */
	public VkPipelineBindPoint type() {
		return type;
	}

	/**
	 * @return Layout of this pipeline
	 */
	public PipelineLayout layout() {
		return layout;
	}

	/**
	 * @return Whether this is a parent pipeline
	 * @see VkPipelineCreateFlag#ALLOW_DERIVATIVES
	 */
	public boolean isParent() {
		return parent;
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		final Library library = this.device().library();
		return buffer -> library.vkCmdBindPipeline(buffer, type, Pipeline.this);
	}

	@Override
	protected Destructor<Pipeline> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyPipeline;
	}

	/**
	 * Pipeline API.
	 */
	interface Library {
		/**
		 * Creates an array of graphics pipelines.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline(s)
		 */
		VkResult vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, @Updated Handle[] pPipelines);

		/**
		 * Creates an array of compute pipelines.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline(s)
		 */
		//VkResult vkCreateComputePipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkComputePipelineCreateInfo[] pCreateInfos, Handle pAllocator, @Updated Handle[] pPipelines);

		/**
		 * Destroys a pipeline.
		 * @param device			Logical device
		 * @param pipeline			Pipeline
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipeline(LogicalDevice device, Pipeline pipeline, Handle pAllocator);

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
		void vkCmdPipelineBarrier(Buffer commandBuffer, EnumMask<VkPipelineStageFlags> srcStageMask, EnumMask<VkPipelineStageFlags> dstStageMask, EnumMask<VkDependencyFlags> dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
	}
}
