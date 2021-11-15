package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.VkPushConstantRange;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.DescriptorLayout;
import org.sarge.jove.util.IntegerEnumeration;
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
	private PipelineLayout(Pointer handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<PipelineLayout> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipelineLayout;
	}

	/**
	 * A <i>push constant range</i>
	 * TODO
	 */
	public static class PushConstantRange {
		private final Set<VkPipelineStage> stages;
		private final int size;
		private final int offset;

		/**
		 * Constructor.
		 * @param stages		Pipeline stages that <b>can</b> access this push constant range
		 * @param size			Size of the data (bytes)
		 * @param offset		Offset (bytes)
		 */
		public PushConstantRange(Set<VkPipelineStage> stages, int size, int offset) {
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

	/**
	 * TODO
	 */
	public class UpdateCommand implements Command {
		private final int mask;
		private final int offset;

		private byte[] data;

		/**
		 * Constructor.
		 * @param stages
		 * @param offset
		 */
		public UpdateCommand(Set<VkPipelineStage> stages, int offset) {
			this.mask = IntegerEnumeration.mask(stages);
			this.offset = zeroOrMore(offset);
		}

		/**
		 * Sets the push constant data for this update.
		 * @param data Push constant data
		 */
		public void write(byte[] data) {
			this.data = notNull(data);
		}

		@Override
		public void execute(VulkanLibrary lib, Buffer buffer) {
			if(data == null) throw new IllegalStateException("Push constant data has not been populated");
			lib.vkCmdPushConstants(buffer, PipelineLayout.this, mask, data.length, offset, data);
		}
	}
	// TODO - is a command created from a range (or a subset of a range)?

	/**
	 * Builder for a pipeline layout.
	 */
	public static class Builder {
		private final List<DescriptorLayout> sets = new ArrayList<>();
		private final List<PushConstantRange> ranges = new ArrayList<>();

		/**
		 * Adds a descriptor-set to this layout.
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
			return this;
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
			info.pSetLayouts = NativeObject.toArray(sets);

			// Add push constant ranges
			info.pushConstantRangeCount = ranges.size();
// TODO - memory error!!!
//			info.pPushConstantRanges = StructureCollector.toPointer(ranges, VkPushConstantRange::new, PushConstantRange::populate);

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference layout = lib.factory().pointer();
			check(lib.vkCreatePipelineLayout(dev, info, null, layout));

			// Create layout
			return new PipelineLayout(layout.getValue(), dev);
		}
	}
}
