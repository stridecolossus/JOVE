package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
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
			validate();
		}
	}

	private void validate() {
    	final var limits = this.device().limits();
    	final int max = limits.maxPushConstantsSize;
    	for(Range range : constant.ranges()) {
    		if(range.size() > max) {
    			throw new IllegalArgumentException("Push constant range %s is larger than device limit %d".formatted(range, max));
    		}
    	}
	}

	/**
	 * @return Push constant for this layout
	 */
	public PushConstant constant() {
		return constant;
	}

	/**
	 * A <i>push constant update command</i> updates a segment of the backing memory for a the push constants of this layout.
	 */
	public class PushConstantUpdateCommand implements Command {
		private final Range range;

		/**
		 * Constructor.
		 * @param range Push constant range to update
		 * @throws IllegalArgumentException if {@link #constant} does not belong to this pipeline layout
		 */
		public PushConstantUpdateCommand(Range range) {
			if(!constant.ranges().contains(range)) {
				throw new IllegalArgumentException("Invalid push constant range: " + range);
			}
			this.range = requireNonNull(range);
		}

		/**
		 * Constructor to update the entire push constant buffer.
		 * @throws NoSuchElementException if this layout does not contain push constants
		 */
		public PushConstantUpdateCommand() {
			// Determine overall length of backing buffer
			final Range last = constant.ranges().getLast();
			final int length = last.offset() + last.size();

			// Aggregate shader stages
			final Set<VkShaderStage> stages = constant
					.ranges()
					.stream()
					.map(Range::stages)
					.flatMap(Set::stream)
					.distinct()
					.collect(toSet());

			// Init synthetic range for whole buffer
			this.range = new Range(0, length, stages);
		}

		@Override
		public void execute(Buffer buffer) {
			final var layout = PipelineLayout.this;
			final PipelineLayout.Library library = layout.device().library();
			library.vkCmdPushConstants(buffer, layout, new EnumMask<>(range.stages()), range.offset(), range.size(), new Handle(constant.data()));
		}

		@Override
		public String toString() {
			return String.format("PushConstantUpdateCommand[%s]", range);
		}
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
			sets.add(layout);
			return this;
		}

		/**
		 * Sets the push constants used by this layout.
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
		 * @throws IllegalArgumentException if the overall length of the push constant ranges exceeds the hardware limit
		 */
		public PipelineLayout build(LogicalDevice device) {
			// Init pipeline layout descriptor
			final var info = new VkPipelineLayoutCreateInfo();
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
			final Pointer handle = new Pointer();
			library.vkCreatePipelineLayout(device, info, null, handle);

			// Create layout
			return new PipelineLayout(handle.get(), device, constant);
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
		void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, EnumMask<VkShaderStage> stageFlags, int offset, int size, Handle pValues);
	}
}
