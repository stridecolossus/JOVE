package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>frame buffer</i> is the target for a {@link RenderPass}.
 * @author Sarge
 * TODO - opaque handle, i.e. no functionality?
 */
public class FrameBuffer extends LogicalDeviceHandle {
	/**
	 * Constructor.
	 * @param handle Handle
	 */
	FrameBuffer(Pointer handle, LogicalDevice dev) {
		super(handle, dev, lib -> lib::vkDestroyFramebuffer);
	}

	/**
	 * Builder for a frame buffer.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final RenderPass pass;
		private final List<Pointer> views = new ArrayList<>();
		private Dimensions extent;
		private int layers = 1;

		/**
		 * Constructor.
		 * @param dev		Device
		 * @param pass		Render pass
		 */
		public Builder(LogicalDevice dev, RenderPass pass) {
			this.dev = notNull(dev);
			this.pass = notNull(pass);
		}

		/**
		 * Adds an image view.
		 * @param view Image view
		 */
		public Builder view(ImageView view) {
			views.add(view.handle());
			return this;
		}

		/**
		 * Sets the extent of the buffer.
		 * @param extent Extent
		 */
		public Builder extent(Dimensions extent) {
			this.extent = notNull(extent);
			return this;
		}

		/**
		 * Sets the number of image layers.
		 * @param layers Number of layers
		 */
		public Builder layers(int layers) {
			this.layers = oneOrMore(layers);
			return this;
		}

		/**
		 * Constructs this frame buffer.
		 * @return New frame buffer
		 * @throws IllegalArgumentException if the frame buffer is not complete
		 */
		public FrameBuffer build() {
			// Validate
			if(views.isEmpty()) throw new IllegalArgumentException("No images views attached");
			if(pass == null) throw new IllegalArgumentException("No render pass specified");
			if(extent == null) throw new IllegalArgumentException("No frame buffer extent specified");

			// Build descriptor
			final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
			info.renderPass = pass.handle();
			info.attachmentCount = views.size();
			info.pAttachments = StructureHelper.pointers(views);
			info.width = extent.width;
			info.height = extent.height;
			info.layers = layers;

			// Allocate frame buffer
			final Vulkan vulkan = dev.vulkan();
			final VulkanLibrary lib = vulkan.api();
			final PointerByReference buffer = vulkan.factory().reference();
			check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

			// Create frame buffer
			return new FrameBuffer(buffer.getValue(), dev);
		}
	}
}
