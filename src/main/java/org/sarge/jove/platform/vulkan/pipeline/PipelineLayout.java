package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.pipeline.PushConstant.Range;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline layout</i> specifies the resources used by a pipeline.
 * @author Sarge
 */
public class PipelineLayout extends AbstractVulkanObject {
	private final PushConstant push;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Logical device
	 * @param push			Push constants
	 */
	PipelineLayout(Handle handle, DeviceContext dev, PushConstant push) {
		super(handle, dev);
		this.push = notNull(push);
	}

	/**
	 * @return Push constants for this layout
	 */
	public PushConstant push() {
		return push;
	}

	@Override
	protected Destructor<PipelineLayout> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipelineLayout;
	}

	/**
	 * Builder for a pipeline layout.
	 */
	public static class Builder {
		private final List<DescriptorLayout> sets = new ArrayList<>();
		private final List<Range> ranges = new ArrayList<>();

		/**
		 * Adds a descriptor set layout to this pipeline.
		 * @param layout Descriptor set layout
		 */
		public Builder add(DescriptorLayout layout) {
			Check.notNull(layout);
			sets.add(layout);
			return this;
		}

		/**
		 * Adds a push constant range to this layout.
		 * @param range Push constant range
		 */
		public Builder add(Range range) {
			Check.notNull(range);
			ranges.add(range);
			return this;
		}

		/**
		 * Constructs this pipeline layout.
		 * @param dev Logical device
		 * @return New pipeline layout
		 * @throws IllegalArgumentException if the overall length of the push constant ranges exceeds the hardware limit
		 */
		public PipelineLayout build(DeviceContext dev) {
			// Init pipeline layout descriptor
			final var info = new VkPipelineLayoutCreateInfo();

			// Add descriptor set layouts
			info.setLayoutCount = sets.size();
			info.pSetLayouts = NativeObject.array(sets);

			// Add push constant ranges
			final var push = new PushConstant(ranges);
			final int len = push.length();
			if(len > 0) {
				// Check that overall size is supported by the hardware
				final int max = dev.limits().value("maxPushConstantsSize");
				if(len > max) throw new IllegalArgumentException(String.format("Push constant buffer too large: max=%d len=%d ", max, len));

				// Add push constant ranges
				info.pushConstantRangeCount = ranges.size();
				info.pPushConstantRanges = StructureCollector.pointer(ranges, new VkPushConstantRange(), Range::populate);
			}

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreatePipelineLayout(dev, info, null, ref));

			// Create layout
			return new PipelineLayout(new Handle(ref), dev, push);
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
		int vkCreatePipelineLayout(DeviceContext device, VkPipelineLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineLayout);

		/**
		 * Destroys a pipeline layout.
		 * @param device			Logical device
		 * @param pPipelineLayout	Pipeline layout
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipelineLayout(DeviceContext device, PipelineLayout pipelineLayout, Pointer pAllocator);

		/**
		 * Updates a push constant range.
		 * @param commandBuffer			Command buffer
		 * @param layout				Pipeline layout
		 * @param stageFlags			Stage flags (mask)
		 * @param offset				Start of the range (bytes)
		 * @param size					Size of the range (bytes)
		 * @param pValues				Push constants data buffer
		 */
		void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, BitMask<VkShaderStage> stageFlags, int offset, int size, ByteBuffer pValues);
	}
}
