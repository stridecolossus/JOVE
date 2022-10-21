package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.StructureCollector;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline layout</i> specifies the resources used by a pipeline.
 * @author Sarge
 */
public class PipelineLayout extends AbstractVulkanObject {
	private final int push;
	private final Set<VkShaderStage> stages;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Logical device
	 * @param push			Push constants buffer length
	 * @param stages		Pipeline shader stages for push constants
	 */
	PipelineLayout(Handle handle, DeviceContext dev, int push, Set<VkShaderStage> stages) {
		super(handle, dev);
		this.push = zeroOrMore(push);
		this.stages = Set.copyOf(stages);
	}

	/**
	 * @return Size of the push constants buffer for this layout
	 */
	public int pushConstantsSize() {
		return push;
	}

	/**
	 * @return Push constant pipeline stages
	 */
	Set<VkShaderStage> stages() {
		return stages;
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
		private final List<PushConstantRange> ranges = new ArrayList<>();
		private int size;

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
		public Builder add(PushConstantRange range) {
			ranges.add(notNull(range));
			size = Math.max(size, range.offset() + range.length());
			return this;
		}

		/**
		 * Adds a push constant range to this layout.
		 * @param layout		Data layout
		 * @param stages		Pipeline stages that can access this range
		 */
		public Builder push(ByteSized layout, Set<VkShaderStage> stages) {
			return add(new PushConstantRange(size, layout.stride(), stages));
		}

		/**
		 * @see #push(int, Set)
		 */
		public Builder push(ByteSized layout, VkShaderStage... stages) {
			final var set = Set.copyOf(Arrays.asList(stages));
			return push(layout, set);
		}

		/**
		 * Constructs this pipeline layout.
		 * @param dev Logical device
		 * @return New pipeline layout
		 */
		public PipelineLayout build(DeviceContext dev) {
			// Init pipeline layout descriptor
			final var info = new VkPipelineLayoutCreateInfo();

			// Add descriptor set layouts
			info.setLayoutCount = sets.size();
			info.pSetLayouts = NativeObject.array(sets);

			// Add push constant ranges
			info.pushConstantRangeCount = ranges.size();
			info.pPushConstantRanges = StructureCollector.pointer(ranges, new VkPushConstantRange(), PushConstantRange::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreatePipelineLayout(dev, info, null, ref));

			// Check that overall size is supported by the hardware
			final int max = dev.limits().value("maxPushConstantsSize");
			if(size > max) throw new IllegalArgumentException(String.format("Push constant size too large: size=%d max=%d", size, max));

			// Enumerate all pipeline stages for push constants
			final Set<VkShaderStage> stages = ranges
					.stream()
					.map(PushConstantRange::stages)
					.flatMap(Set::stream)
					.collect(toSet());

			// Create layout
			return new PipelineLayout(Handle.of(ref), dev, size, stages);
		}
	}

	/**
	 * Pipeline layout API.
	 */
	public interface Library {
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
		 * Updates push constants.
		 * @param commandBuffer			Command buffer
		 * @param layout				Pipeline layout
		 * @param stageFlags			Stage flags (mask)
		 * @param offset				Start of the range (bytes)
		 * @param size					Size of the push constants (bytes)
		 * @param pValues				Push constants as an array of bytes
		 */
		void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, int stageFlags, int offset, int size, ByteBuffer pValues);
	}
}
