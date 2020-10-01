package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.util.ExtentHelper;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> specifies how attachments are managed during rendering.
 * @author Sarge
 */
public class RenderPass extends AbstractVulkanObject {
	/**
	 * End render pass command.
	 */
	public static final Command END_COMMAND = (api, buffer) -> api.vkCmdEndRenderPass(buffer);

	/**
	 * Index of the implicit sub-pass before or after the render pass.
	 */
	public static final int VK_SUBPASS_EXTERNAL = (~0);

	/**
	 * Constructor.
	 * @param handle		Render pass handle
	 * @param dev			Logical device
	 */
	RenderPass(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyRenderPass);
	}

	/**
	 * Creates a command to begin rendering.
	 * @param buffer		Frame buffer
	 * @param extent		Extent
	 * @param col			Clear colour
	 * @return Begin rendering command
	 * @throws NullPointerException if any clear colour element is {@code null}
	 */
	public Command begin(FrameBuffer buffer, Rectangle extent, Colour col) {		// TODO - args should be properties: fb.extent, pass.clear[]
		// Create descriptor
		final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
		info.renderPass = this.handle();
		info.framebuffer = buffer.handle();
		info.renderArea = ExtentHelper.of(extent);

		// Create clear colour
		final VkClearValue clear = new VkClearValue();
		clear.color = new VkClearColorValue();									// TODO - by value?
		clear.color.float32 = col.toArray();

		// Add clear values
		info.clearValueCount = 1;
		info.pClearValues = StructureHelper.structures(List.of(clear));

		// Create command
		return (lib, ptr) -> lib.vkCmdBeginRenderPass(ptr, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE);
	}

	/**
	 * Builder for a render pass.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final List<VkAttachmentDescription> attachments = new ArrayList<>();
		private final List<VkSubpassDescription> subpasses = new ArrayList<>();
		private final List<VkSubpassDependency> dependencies = new ArrayList<>();			// TODO - dependencies builder

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * @return New attachment builder
		 */
		public AttachmentBuilder attachment() {
			return new AttachmentBuilder();
		}

		/**
		 * @return New sub-pass builder
		 */
		public SubpassBuilder subpass() {
			return new SubpassBuilder();
		}

		/**
		 * Constructs this render pass.
		 * @return New render pass
		 */
		public RenderPass build() {
			// Init render pass descriptor
			final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

			// Add attachments
			// TODO - check not empty
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureHelper.structures(attachments);

			// Add sub passes
			// TODO - check not empty
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureHelper.structures(subpasses);

			// Add dependencies
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureHelper.structures(dependencies);

			// Allocate render pass
			final VulkanLibrary lib = dev.library();
			final PointerByReference pass = lib.factory().pointer();
			check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

			// Create render pass
			return new RenderPass(pass.getValue(), dev);
		}

		/**
		 * Builder for a render pass attachment.
		 */
		public class AttachmentBuilder {
			private final VkAttachmentDescription info = new VkAttachmentDescription();

			/**
			 * Constructor.
			 */
			private AttachmentBuilder() {
				info.format = VkFormat.VK_FORMAT_UNDEFINED; 						// TODO - is this valid or should we check in build()?
				info.samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT;
				info.loadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
				info.storeOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
				info.stencilLoadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
				info.stencilStoreOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
				info.initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
				info.finalLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
				// TODO - VkAttachmentDescriptionFlagBits -> flags
			}

			/**
			 * Sets the attachment format (usually the same as the swap-chain).
			 * @param format Attachment format
			 */
			public AttachmentBuilder format(VkFormat format) {
				// TODO - could be undefined?
				info.format = notNull(format);
				return this;
			}

			/**
			 * Sets the number of samples.
			 * @param samples Number of samples
			 */
			public AttachmentBuilder samples(VkSampleCountFlag samples) {
				info.samples = notNull(samples);
				return this;
			}

			/**
			 * Sets the attachment load operation (before rendering).
			 * @param op Load operation
			 */
			public AttachmentBuilder load(VkAttachmentLoadOp op) {
				info.loadOp = notNull(op);
				return this;
			}

			/**
			 * Sets the attachment store operation (after rendering).
			 * @param op Store operation
			 */
			public AttachmentBuilder store(VkAttachmentStoreOp op) {
				info.storeOp = notNull(op);
				return this;
			}

			// TODO - stencil

			/**
			 * Sets the initial image layout.
			 * @param layout Initial image layout
			 */
			public AttachmentBuilder initialLayout(VkImageLayout layout) {
				info.initialLayout = notNull(layout);
				return this;
			}

			/**
			 * Sets the final image layout.
			 * @param layout final image layout
			 */
			public AttachmentBuilder finalLayout(VkImageLayout layout) {
				if((layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) || (layout == VkImageLayout.VK_IMAGE_LAYOUT_PREINITIALIZED)) {
					throw new IllegalArgumentException("Invalid final layout: " + layout);
				}
				info.finalLayout = notNull(layout);
				return this;
			}

			/**
			 * Constructs this attachment.
			 */
			public Builder build() {
				attachments.add(info);
				return Builder.this;
			}
		}

		/**
		 * Builder for a sub-pass.
		 */
		public class SubpassBuilder {
			private final VkSubpassDescription info = new VkSubpassDescription();
			private final List<VkAttachmentReference> colour = new ArrayList<>();
			// TODO - other attachment types

			/**
			 * Constructor.
			 */
			private SubpassBuilder() {
				bind(VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS);
			}

			/**
			 * Sets the bind point of this sub-pass.
			 * @param bind Bind point (default is {@link VkPipelineBindPoint#VK_PIPELINE_BIND_POINT_GRAPHICS})
			 */
			public SubpassBuilder bind(VkPipelineBindPoint bind) {
				info.pipelineBindPoint = notNull(bind);
				return this;
			}

			/**
			 * Adds a colour attachment reference.
			 * @param index			Attachment index
			 * @param layout		Attachment layout
			 */
			public SubpassBuilder colour(int index, VkImageLayout layout) {
				// Validate reference
				// TODO - unused attachment VK_ATTACHMENT_UNUSED
				// TODO - is there some cunning way we can avoid having to use index?
				if(index >= attachments.size()) throw new IllegalArgumentException("Invalid attachment index: " + index);
				if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) throw new IllegalArgumentException("Invalid layout for colour attachment: " + layout);

				// Create reference
				final var ref = new VkAttachmentReference();
				ref.attachment = zeroOrMore(index);
				ref.layout = notNull(layout);

				// Add reference
				colour.add(ref);

				return this;
			}

			/**
			 * Constructs this sub-pass.
			 */
			public Builder build() {
				// Populate colour attachment references
				info.pColorAttachments = StructureHelper.structures(colour);
				info.colorAttachmentCount = colour.size();

				// Add sub-pass
				subpasses.add(info);

				return Builder.this;
			}
		}
	}
}