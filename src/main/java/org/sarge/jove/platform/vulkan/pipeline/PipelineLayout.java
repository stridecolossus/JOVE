package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>pipeline layout</i> specifies the resources used by a pipeline.
 * @author Sarge
 */
public class PipelineLayout extends VulkanObject {
	private final PushConstant constant;

	/**
	 * Constructor.
	 * @param handle		Layout handle
	 * @param device		Logical device
	 * @param constant		Optional push constant
	 * @throws IllegalArgumentException if any range of the push constant exceeds the maximum for the device
	 */
	PipelineLayout(Handle handle, LogicalDevice device, PushConstant constant) {
		super(handle, device);
		this.constant = constant;
		if(constant != null) {
			constant.validate(device);
		}
	}

	/**
	 * @return Push constant for this layout
	 */
	public PushConstant constant() {
		return constant;
	}

	@Override
	protected Destructor<PipelineLayout> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyPipelineLayout;
	}

	/**
	 * Builder for a pipeline layout.
	 */
	public static class Builder {
		private final List<DescriptorSet.Layout> sets = new ArrayList<>();
		private PushConstant constant;

		/**
		 * Adds a descriptor set layout to this pipeline.
		 * @param layout Descriptor set layout
		 */
		public Builder add(DescriptorSet.Layout layout) {
			requireNonNull(layout);
			sets.add(layout);
			return this;
		}

		/**
		 * Sets the push constant used by this layout.
		 * @param constant Push constant
		 */
		public Builder constant(PushConstant constant) {
			this.constant = constant;
			return this;
		}

		/**
		 * Constructs this pipeline layout.
		 * @param device Logical device
		 * @return New pipeline layout
		 */
		public PipelineLayout build(LogicalDevice device) {
			// Init pipeline layout descriptor
			final var info = new VkPipelineLayoutCreateInfo();
			info.sType = VkStructureType.PIPELINE_LAYOUT_CREATE_INFO;
			info.flags = 0;

			// Add descriptor set layouts
			info.setLayoutCount = sets.size();
			info.pSetLayouts = NativeObject.handles(sets);

			// Add push constant ranges
			if(constant != null) {
    			info.pushConstantRangeCount = constant.ranges().size();
    			info.pPushConstantRanges = constant
    					.ranges()
    					.stream()
    					.map(Range::populate)
    					.toArray(VkPushConstantRange[]::new);
			}

			// Allocate layout
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreatePipelineLayout(device, info, null, pointer);

			// Create layout
			return new PipelineLayout(pointer.handle(), device, constant);
		}
	}

	/**
	 * Pipeline layout API.
	 */
	interface Library {
		/**
		 * Creates a pipeline layout.
		 * @param device			Logical device
		 * @param pCreateInfo		Pipeline layout descriptor
		 * @param pAllocator		Allocator
		 * @param pPipelineLayout	Returned pipeline layout
		 * @return Result
		 */
		VkResult vkCreatePipelineLayout(LogicalDevice device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, Pointer pPipelineLayout);

		/**
		 * Destroys a pipeline layout.
		 * @param device			Logical device
		 * @param pPipelineLayout	Pipeline layout
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipelineLayout(LogicalDevice device, PipelineLayout pipelineLayout, Handle pAllocator);

		/**
		 * Updates a push constant range.
		 * @param commandBuffer			Command buffer
		 * @param layout				Pipeline layout
		 * @param stageFlags			Stage flags
		 * @param offset				Start of the range (bytes)
		 * @param size					Size of the range (bytes)
		 * @param pValues				Push constants data buffer
		 */
		void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStageFlags> stageFlags, int offset, int size, Handle pValues);
	}
}
