package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"flags",
	"format",
	"samples",
	"loadOp",
	"storeOp",
	"stencilLoadOp",
	"stencilStoreOp",
	"initialLayout",
	"finalLayout"
})
public class VkAttachmentDescription extends Structure {
	public static class ByValue extends VkAttachmentDescription implements Structure.ByValue { }
	public static class ByReference extends VkAttachmentDescription implements Structure.ByReference {
		@Override
		protected void useMemory(Pointer m) {
			super.useMemory(m);
		}
	}

	public int flags;
	public int format;
	public int samples;
	public int loadOp;
	public int storeOp;
	public int stencilLoadOp;
	public int stencilStoreOp;
	public int initialLayout;
	public int finalLayout;

	/**
	 * Builder for an attachment descriptor.
	 */
	public static class Builder {
		private VkSampleCountFlag samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT;
		private VkFormat format = VkFormat.VK_FORMAT_UNDEFINED;
		private VkAttachmentLoadOp loadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
		private VkAttachmentStoreOp storeOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE;
		private VkAttachmentLoadOp stencilLoadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
		private VkAttachmentStoreOp stencilStoreOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
		private VkImageLayout initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
		private VkImageLayout finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;

		public Builder format(VkFormat format) {
			this.format = format;
			return this;
		}

		public Builder samples(VkSampleCountFlag samples) {
			this.samples = samples;
			return this;
		}

		public Builder load(VkAttachmentLoadOp op) {
			this.loadOp = op;
			return this;
		}

		public Builder store(VkAttachmentStoreOp op) {
			this.storeOp = op;
			return this;
		}

		public Builder stencilLoad(VkAttachmentLoadOp op) {
			this.stencilLoadOp = op;
			return this;
		}

		public Builder stencilStore(VkAttachmentStoreOp op) {
			this.stencilStoreOp = op;
			return this;
		}

		public Builder initialLayout(VkImageLayout layout) {
			this.initialLayout = layout;
			return this;
		}

		public Builder finalLayout(VkImageLayout layout) {
			this.finalLayout = layout;
			return this;
		}

		/**
		 * Constructs this attachment description.
		 * @return New attachment descriptor
		 */
		public VkAttachmentDescription build() {
			final VkAttachmentDescription desc = new VkAttachmentDescription();
			desc.format = format.value();
			desc.samples = samples.value();
			desc.loadOp = loadOp.value();
			desc.storeOp = storeOp.value();
			desc.stencilLoadOp = stencilLoadOp.value();
			desc.stencilStoreOp = stencilStoreOp.value();
			desc.initialLayout = initialLayout.value();
			desc.finalLayout = finalLayout.value();
			return desc;
		}
	}
}
