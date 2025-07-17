package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.CommandBuffer;
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
	 * @param dev			Logical device
	 * @param type			Type of pipeline
	 * @param layout		Pipeline layout
	 * @param parent		Whether this is a parent pipeline
	 */
	Pipeline(Handle handle, LogicalDevice dev, VkPipelineBindPoint type, PipelineLayout layout, boolean parent) {
		super(handle, dev);
		this.type = requireNonNull(type);
		this.layout = requireNonNull(layout);
		this.parent = parent;
	}

	/**
	 * @return Type of this pipeline (or the bind point)
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
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, type, Pipeline.this);
	}

	@Override
	protected Destructor<Pipeline> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipeline;
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
		 * @return Result
		 */
		int vkCreateGraphicsPipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines);

		/**
		 * Creates an array of compute pipelines.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline(s)
		 * @return Result
		 */
		int vkCreateComputePipelines(LogicalDevice device, PipelineCache pipelineCache, int createInfoCount, VkComputePipelineCreateInfo[] pCreateInfos, Handle pAllocator, Handle[] pPipelines);

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
		void vkCmdBindPipeline(CommandBuffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline);

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
		void vkCmdPipelineBarrier(CommandBuffer commandBuffer, EnumMask<VkPipelineStage> srcStageMask, EnumMask<VkPipelineStage> dstStageMask, EnumMask<VkDependencyFlag> dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
	}
}
