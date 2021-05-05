package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineStageFlag;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.util.StructureCollector;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline layout</i> specifies the resources used by a pipeline.
 * @author Sarge
 */
public class PipelineLayout extends AbstractVulkanObject {
	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Logical device
	 */
	private PipelineLayout(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyPipelineLayout);
	}

	/**
	 * A <i>push constant range</i>
	 * TODO
	 */
	public static class PushConstantRange {
		private final Set<VkPipelineStageFlag> stages;
		private final int size;
		private final int offset;

		/**
		 * Constructor.
		 * @param stages		Pipeline stages that <b>can</b> access this push constant range
		 * @param size			Size of the data (bytes)
		 * @param offset		Offset (bytes)
		 */
		public PushConstantRange(Set<VkPipelineStageFlag> stages, int size, int offset) {
			this.stages = Set.copyOf(notEmpty(stages));
			this.size = oneOrMore(size);
			this.offset = zeroOrMore(offset);
		}

//		public UpdateCommand update(Set<VkPipelineStageFlag> stages, int size, int offset) {
//			return new UpdateCommand(stages, size, offset);
//		}
//
//		public UpdateCommand update(int size) {
//			return update(this.stages, size, 0);
//		}

		/**
		 * Populates a push constant range structure.
		 */
		void populate(VkPushConstantRange range) {
			range.stageFlags = IntegerEnumeration.mask(stages);
			range.size = size;
			range.offset = offset;
		}
	}

	public class UpdateCommand implements Command {
		private final int mask;
		private final ByteBuffer buffer;
		private final int offset;

		/**
		 * Constructor.
		 * @param mask
		 * @param buffer
		 * @param offset
		 */
		public UpdateCommand(Set<VkPipelineStageFlag> stages, int size, int offset) {
			this.mask = IntegerEnumeration.mask(stages);
			this.buffer = VulkanHelper.buffer(size);
			this.offset = offset;
		}

		public void write(Bufferable data) {
			data.buffer(buffer);
		}

		@Override
		public void execute(VulkanLibrary lib, Handle handle) {
			lib.vkCmdPushConstants(handle, PipelineLayout.this.handle(), mask, buffer.limit(), offset, buffer);
		}
	}

	/**
	 * Builder for a pipeline layout.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final List<DescriptorSet.Layout> sets = new ArrayList<>();
		private final List<PushConstantRange> ranges = new ArrayList<>();

		/**
		 * Constructor.
		 * @param dev			Logical device
		 * @param parent		Parent builder
		 * @param consumer		Consumer
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Adds a descriptor-set to this layout.
		 * @param layout Descriptor set layout
		 */
		public Builder add(DescriptorSet.Layout layout) {
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
			return this;
		}

		/**
		 * Constructs this pipeline layout.
		 * @return New pipeline layout
		 */
		public PipelineLayout build() {
			// Init pipeline layout descriptor
			final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();

			// Add descriptor set layouts
			info.setLayoutCount = sets.size();
			info.pSetLayouts = Handle.toArray(sets);

			// Add push constant ranges
			info.pushConstantRangeCount = ranges.size();
			info.pPushConstantRanges = StructureCollector.toPointer(ranges, VkPushConstantRange::new, PushConstantRange::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference layout = lib.factory().pointer();
			check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));

			// Create layout
			return new PipelineLayout(layout.getValue(), dev);
		}
	}
}
