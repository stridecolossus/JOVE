package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> specifies how attachments are managed during rendering.
 * @author Sarge
 * TODO
 * - lots of nasty indices, better to refer to actual objects? => less checking needed, more explicit
 * - subpass dependencies restricted to current subpass builder, too restrictive?
 */
public class RenderPass extends VulkanHandle {
	/**
	 * End render pass command.
	 */
	public static final Command END_COMMAND = (lib, ptr) -> lib.vkCmdEndRenderPass(ptr);

	/**
	 * Index of the implicit sub-pass before or after the render pass.
	 */
	public static final int VK_SUBPASS_EXTERNAL = (~0);

	/**
	 * Constructor.
	 * @param handle Handle
	 */
	RenderPass(VulkanHandle handle) {
		super(handle);
	}

	/**
	 * Creates a command to begin rendering.
	 * @param fb		Frame buffer
	 * @param extent	Extent
	 * @param clear		Clear colour(s)
	 * @return Begin rendering command
	 * @throws NullPointerException if any clear colour element is {@code null}
	 */
	public Command begin(FrameBuffer fb, Rectangle extent, Colour[] clear) {
		// Create descriptor
		final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
		info.renderPass = this.handle();
		info.framebuffer = fb.handle();
		info.renderArea = new VkRect2D(extent);
		info.clearValueCount = clear.length;
		info.pClearValues = Bufferable.create(clear);

		// Create command
		return (lib, ptr) -> lib.vkCmdBeginRenderPass(ptr, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE); // TODO - secondary
	}

	/**
	 * Builder for a render pass.
	 */
	public static class Builder {
		/**
		 * Builder for an attachment descriptor.
		 */
		public class AttachmentBuilder {
			private VkAttachmentDescription attachment = new VkAttachmentDescription();

			private AttachmentBuilder() {
			}

			/**
			 * Sets the attachment format.
			 * @param format Format
			 */
			public AttachmentBuilder format(VkFormat format) {
				attachment.format = notNull(format);
				return this;
			}

			/**
			 * Sets the sample count.
			 * @param samples Sample count
			 */
			public AttachmentBuilder samples(VkSampleCountFlag samples) {
				attachment.samples = notNull(samples);
				return this;
			}

			/**
			 * Sets the attachment load operation.
			 * @param op Load operation
			 */
			public AttachmentBuilder load(VkAttachmentLoadOp op) {
				attachment.loadOp = notNull(op);
				return this;
			}

			/**
			 * Sets the attachment store operation.
			 * @param op Store operation
			 */
			public AttachmentBuilder store(VkAttachmentStoreOp op) {
				attachment.storeOp = notNull(op);
				return this;
			}

			/**
			 * Sets the attachment stencil load operation.
			 * @param op Stencil load operation
			 */
			public AttachmentBuilder stencilLoad(VkAttachmentLoadOp op) {
				attachment.stencilLoadOp = notNull(op);
				return this;
			}

			/**
			 * Sets the attachment stencil store operation.
			 * @param op Stencil store operation
			 */
			public AttachmentBuilder stencilStore(VkAttachmentStoreOp op) {
				attachment.stencilStoreOp = notNull(op);
				return this;
			}

			/**
			 * Sets the initial layout of this attachment.
			 * @param layout Initial layout
			 */
			public AttachmentBuilder initialLayout(VkImageLayout layout) {
				attachment.initialLayout = notNull(layout);
				return this;
			}

			/**
			 * Sets the final layout of this attachment.
			 * @param layout Final layout
			 */
			public AttachmentBuilder finalLayout(VkImageLayout layout) {
				attachment.finalLayout = notNull(layout);
				return this;
			}

			/**
			 * Constructs this attachment description.
			 * @return Parent builder
			 */
			public Builder build() {
				attachments.add(attachment);
				return Builder.this;
			}
		}

		/**
		 * Builder for a sub-pass.
		 */
		public class SubpassBuilder {
			private final VkSubpassDescription subpass = new VkSubpassDescription();
			private final List<VkAttachmentReference> colour = new ArrayList<>();
			private final int index;

			/**
			 * Constructor.
			 * @param index Sub-pass index
			 */
			private SubpassBuilder(int index) {
				this.index = index;
			}

			/**
			 * Adds a colour attachment.
			 * @param index Attachment index
			 * TODO - nasty index, how to validate?
			 * TODO - always colour attachment?
			 */
			public SubpassBuilder attachment(int index) {
				final VkAttachmentReference ref = new VkAttachmentReference();
				ref.attachment = index;
				ref.layout = VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
				colour.add(ref);
				return this;
			}

			// TODO - others

			/**
			 * Adds a new dependency.
			 * @return New dependency
			 */
			public DependencyBuilder dependency() {
				return new DependencyBuilder(this, index);
			}

			/**
			 * Constructs this sub-pass.
			 * @return Parent builder
			 */
			public Builder build() {
				// Add colour attachments
				subpass.pColorAttachments = StructureHelper.structures(colour);
				subpass.colorAttachmentCount = colour.size();

				// TODO - others

				// Add sub-pass
				subpasses.add(subpass);
				return Builder.this;
			}
		}

		/**
		 * Builder for a sub-pass dependency.
		 */
		public class DependencyBuilder {
			private final SubpassBuilder parent;
			private final int index;
			private final VkSubpassDependency info = new VkSubpassDependency();

			/**
			 * Constructor.
			 * @param builder		Parent builder
			 * @param index			Index of current/max sub-pass
			 */
			private DependencyBuilder(SubpassBuilder builder, int index) {
				this.parent = builder;
				this.index = index;
				info.srcSubpass = VK_SUBPASS_EXTERNAL;
			}

			/**
			 * Sets the source sub-pass.
			 * @param src Source sub-pass
			 */
			public DependencyBuilder source(int subpass) {
				// TODO - this is probably too restrictive?
				Check.zeroOrMore(subpass);
				if(subpass >= index) throw new IllegalArgumentException(String.format("Invalid source index: subpass=%d max=%d", subpass, index));
				info.srcSubpass = subpass;
				return this;
			}

			/**
			 * Add a source stage dependency.
			 * @param stage Source stage
			 */
			public DependencyBuilder sourceStage(VkPipelineStageFlag stage) {
				info.srcStageMask |= stage.value();
				return this;
			}

			/**
			 * Adds a source access flag.
			 * @param access Source access
			 */
			public DependencyBuilder sourceAccess(VkAccessFlag access) {
				info.srcAccessMask |= access.value();
				return this;
			}

			/**
			 * Adds a destination stage dependency.
			 * @param stage Destination stage
			 */
			public DependencyBuilder destinationStage(VkPipelineStageFlag stage) {
				info.dstStageMask |= stage.value();
				return this;
			}

			/**
			 * Adds a destination access flag.
			 * @param access Destination access
			 */
			public DependencyBuilder destinationAccess(VkAccessFlag access) {
				info.dstAccessMask |= access.value();
				return this;
			}

			/**
			 * Constructs this dependency.
			 * @return Parent sub-pass builder
			 */
			public SubpassBuilder build() {
				// TODO - actually construct here so cannot fiddle afterwards
				Builder.this.dependencies.add(info);
				return parent;
			}
		}

		private final LogicalDevice dev;
		private final List<VkAttachmentDescription> attachments = new ArrayList<>();
		private final List<VkSubpassDescription> subpasses = new ArrayList<>();
		private final List<VkSubpassDependency> dependencies = new ArrayList<>();

		/**
		 * Constructor.
		 * @param dev Device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
		}

		/**
		 * Starts a new attachment.
		 * @return Attachment builder
		 */
		public AttachmentBuilder attachment() {
			return new AttachmentBuilder();
		}

		/**
		 * Starts a new sub-pass.
		 * @return Sub-pass builder
		 */
		public SubpassBuilder subpass() {
			return new SubpassBuilder(subpasses.size());
		}

		/**
		 * Constructs this render pass.
		 * @param device Logical device
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

			// Create render pass
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference pass = vulkan.factory().reference();
			check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

			// Create render pass wrapper
			final Pointer handle = pass.getValue();
			final Destructor destructor = () -> lib.vkDestroyRenderPass(dev.handle(), handle, null);
			return new RenderPass(new VulkanHandle(handle, destructor));
		}
	}
}
