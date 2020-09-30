package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>pipeline layout</i> specifies the descriptor sets and push constants used in a pipeline.
 */
public class PipelineLayout {
	private final Handle handle;
	private final LogicalDevice dev;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Logical device
	 */
	private PipelineLayout(Pointer handle, LogicalDevice dev) {
		this.handle = new Handle(handle);
		this.dev = notNull(dev);
	}

	public Handle handle() {
		return handle;
	}

	/**
	 * Destroys this pipeline layout.
	 */
	public void destroy() {
		dev.library().vkDestroyPipelineLayout(dev.handle(), handle, null);
	}

	/**
	 * Builder for a pipeline layout.
	 */
	public static class Builder extends AbstractPipelineStageBuilder<PipelineLayout> {
		private final LogicalDevice dev;
		//private final List<Pointer> sets = new StrictList<>();
		// TODO - push constant layouts

		/**
		 * Constructor.
		 * @param dev			Logical device
		 * @param parent		Parent builder
		 * @param consumer		Consumer
		 */
		Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

//		/**
//		 * Adds a descriptor set layout.
//		 * @param layout Descriptor set layout
//		 */
//		public Builder add(DescriptorSet.Layout layout) {
//			// TODO - check for duplicates?
//			sets.add(layout.handle());
//			return this;
//		}

		// TODO - push constants

		/**
		 * Constructs this pipeline layout.
		 * @return New pipeline layout
		 */
		@Override
		protected PipelineLayout result() {
			// Init pipeline layout descriptor
			final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();

			// Add descriptor set layouts
//			info.setLayoutCount = sets.size();
//			info.pSetLayouts = StructureHelper.pointers(sets);

			// Add push constants
			// TODO

			// Allocate layout
			final VulkanLibrary lib = dev.library();
			final PointerByReference layout = lib.factory().pointer();
			check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));

			// Create layout
			return new PipelineLayout(layout.getValue(), dev);
		}
	}
}
