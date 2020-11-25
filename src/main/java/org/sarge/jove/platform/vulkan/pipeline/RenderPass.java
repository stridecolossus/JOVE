package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.View;

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

	// TODO - number of attachments & check frame buffer
	// TODO - separate attachment descriptor?

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
	 * @param buffer Frame buffer
	 * @return Begin rendering command
	 */
	public Command begin(FrameBuffer buffer) {
		// Create descriptor
		final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
		info.renderPass = this.handle();
		info.framebuffer = buffer.handle();
		buffer.extents().populate(info.renderArea);

		// Map attachments to clear values
		final Collection<ClearValue> values = buffer.attachments().stream().map(View::clear).collect(toList());

		// Init clear values
		info.clearValueCount = values.size();
		info.pClearValues = VulkanStructure.populate(VkClearValue::new, values, ClearValue::populate);

		// Create command
		return (lib, handle) -> lib.vkCmdBeginRenderPass(handle, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE);
	}

	/**
	 * Builder for a render pass.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final List<AttachmentBuilder> attachments = new ArrayList<>();
		private final List<SubPassBuilder> subpasses = new ArrayList<>();
		private final List<DependencyBuilder> dependencies = new ArrayList<>();

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
		public SubPassBuilder subpass() {
			return new SubPassBuilder();
		}

		/**
		 * Starts a new sub-pass dependency.
		 * <p>
		 * The special case {@link RenderPass#VK_SUBPASS_EXTERNAL} index is used for the implicit sub-pass before or after the render pass.
		 * @param src		Source index
		 * @param dest		Destination index
		 * @return New dependency builder
		 * @throws IllegalArgumentException if the source index is greater-than the destination
		 */
		public DependencyBuilder dependency(int src, int dest) {
			return new DependencyBuilder(src, dest);
		}

		/**
		 * Constructs this render pass.
		 * @return New render pass
		 */
		public RenderPass build() {
			// Create render pass descriptor
			final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

			// Add attachments
			if(attachments.isEmpty()) throw new IllegalArgumentException("At least one attachment must be specified");
			info.attachmentCount = attachments.size();
			info.pAttachments = VulkanStructure.populate(VkAttachmentDescription::new, attachments, AttachmentBuilder::populate);

			// Add sub-passes
			if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
			info.subpassCount = subpasses.size();
			info.pSubpasses = VulkanStructure.populate(VkSubpassDescription::new, subpasses, SubPassBuilder::populate);

			// Add dependencies
			info.dependencyCount = dependencies.size();
			info.pDependencies = VulkanStructure.populate(VkSubpassDependency::new, dependencies, DependencyBuilder::populate);

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
			private VkFormat format;
			private VkSampleCountFlag samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT;
			private VkAttachmentLoadOp loadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
			private VkAttachmentStoreOp storeOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
			private VkAttachmentLoadOp stencilLoadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
			private VkAttachmentStoreOp stencilStoreOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
			private VkImageLayout initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
			private VkImageLayout finalLayout;

			/**
			 * Sets the attachment format (usually the same as the swap-chain).
			 * @param format Attachment format
			 */
			public AttachmentBuilder format(VkFormat format) {
				// TODO - check undefined? or is that valid?
				this.format = notNull(format);
				return this;
			}

			/**
			 * Sets the number of samples.
			 * @param samples Number of samples
			 */
			public AttachmentBuilder samples(VkSampleCountFlag samples) {
				this.samples = notNull(samples);
				return this;
			}

			/**
			 * Sets the attachment load operation (before rendering).
			 * @param op Load operation
			 */
			public AttachmentBuilder load(VkAttachmentLoadOp op) {
				this.loadOp = notNull(op);
				return this;
			}

			/**
			 * Sets the attachment store operation (after rendering).
			 * @param op Store operation
			 */
			public AttachmentBuilder store(VkAttachmentStoreOp op) {
				this.storeOp = notNull(op);
				return this;
			}

			/**
			 * Sets the initial image layout.
			 * @param layout Initial image layout
			 */
			public AttachmentBuilder initialLayout(VkImageLayout layout) {
				this.initialLayout = notNull(layout);
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
				this.finalLayout = notNull(layout);
				return this;
			}

			/**
			 * Populates the descriptor for this attachment.
			 * @param Attachment descriptor
			 */
			void populate(VkAttachmentDescription desc) {
				desc.format = format;
				desc.samples = samples;
				desc.loadOp = loadOp;
				desc.storeOp = storeOp;
				desc.stencilLoadOp = stencilLoadOp;
				desc.stencilStoreOp = stencilStoreOp;
				desc.initialLayout = initialLayout;
				desc.finalLayout = finalLayout;
			}

			/**
			 * Constructs this attachment.
			 * @throws IllegalArgumentException if the attachment format or final layout has not been specified
			 * @throws IllegalArgumentException if the format is invalid for the final layout
			 */
			public Builder build() {
				if(format == null) throw new IllegalArgumentException("No format specified for attachment");
				if(finalLayout == null) throw new IllegalArgumentException("No final layout specified");
				// TODO - validation https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkAttachmentDescription.html
				attachments.add(this);
				return Builder.this;
			}
		}

		/**
		 * Builder for a sub-pass.
		 */
		public class SubPassBuilder {
			/**
			 * Attachment reference.
			 */
			private class Reference {
				private final int index;
				private final VkImageLayout layout;

				/**
				 * Constructor.
				 * @param index			Attachment index
				 * @param layout		Image layout
				 * @throws IllegalArgumentException if the index is invalid for this render pass or the layout is undefined
				 */
				private Reference(int index, VkImageLayout layout) {
					this.index = zeroOrMore(index);
					this.layout = notNull(layout);
					if(index >= attachments.size()) throw new IllegalArgumentException("Invalid attachment index: " + index);
					if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) throw new IllegalArgumentException("Invalid attachment layout: " + layout);
				}

				/**
				 * Populates the descriptor for this attachment reference.
				 * @param ref Attachment reference descriptor
				 */
				private void populate(VkAttachmentReference ref) {
					ref.attachment = index;
					ref.layout = layout;
				}
			}
			// TODO - unused attachment VK_ATTACHMENT_UNUSED
			// TODO - is there some cunning way we can avoid having to use index?

			private VkPipelineBindPoint bind = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS;
			private final List<Reference> colour = new ArrayList<>();
			private Reference depth;

			/**
			 * Sets the bind point of this sub-pass.
			 * @param bind Bind point (default is {@link VkPipelineBindPoint#VK_PIPELINE_BIND_POINT_GRAPHICS})
			 */
			public SubPassBuilder bind(VkPipelineBindPoint bind) {
				this.bind = notNull(bind);
				return this;
			}

			/**
			 * Adds a colour attachment.
			 * @param index			Attachment index
			 * @param layout		Attachment layout
			 */
			public SubPassBuilder colour(int index, VkImageLayout layout) {
				colour.add(new Reference(index, layout));
				return this;
			}

			/**
			 * Adds the depth-buffer attachment.
			 * @param index Attachment index
			 * @throws IllegalArgumentException if the depth buffer has already been configured
			 */
			public SubPassBuilder depth(int index) {
				if(depth != null) throw new IllegalArgumentException("Depth buffer already configured");
				depth = new Reference(index, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
				return this;
			}

			/**
			 * Populates the descriptor for this sub-pass.
			 * @param desc Sub-pass descriptor
			 */
			void populate(VkSubpassDescription desc) {
				// Init descriptor
				desc.pipelineBindPoint = bind;

				// Populate colour attachments
				desc.colorAttachmentCount = colour.size();
				desc.pColorAttachments = VulkanStructure.populate(VkAttachmentReference::new, colour, Reference::populate);

				// Populate depth attachment
				if(depth != null) {
					desc.pDepthStencilAttachment = new VkAttachmentReference();
					depth.populate(desc.pDepthStencilAttachment);
				}
			}

			/**
			 * Constructs this sub-pass.
			 * @throws IllegalArgumentException if no attachment references were specified
			 */
			public Builder build() {
				if((depth == null) && colour.isEmpty()) throw new IllegalArgumentException("No attachments specified in sub-pass");
				subpasses.add(this);
				return Builder.this;
			}
		}

		/**
		 * Builder for a render pass dependency.
		 * <p>
		 * Notes:
		 * <ul>
		 * <li>The special case {@link RenderPass#VK_SUBPASS_EXTERNAL} index specifies the implicit sub-pass before or after the render pass</li>
		 * <li>All dependencies <b>must</b> specify at least one pipeline stage</li>
		 * </ul>
		 */
		public class DependencyBuilder {
			/**
			 * Source or destination sub-pass dependency.
			 */
			public class Dependency {
				private final int index;
				private final Set<VkPipelineStageFlag> stages = new HashSet<>();
				private final Set<VkAccessFlag> access = new HashSet<>();

				/**
				 * Constructor.
				 * @param index Sub-pass index
				 * @throws IllegalArgumentException for an invalid index
				 */
				private Dependency(int index) {
					if((index != VK_SUBPASS_EXTERNAL) && (index >= subpasses.size())) throw new IllegalArgumentException("Invalid sub-pass index: " + index);
					this.index = index;
				}

				/**
				 * Adds a pipeline stage.
				 * @param stage Pipeline stage
				 */
				public DependencyBuilder stage(VkPipelineStageFlag stage) {
					stages.add(notNull(stage));
					return DependencyBuilder.this;
				}

				/**
				 * Adds an access flag.
				 * @param access Access flag
				 */
				public DependencyBuilder access(VkAccessFlag access) {
					this.access.add(notNull(access));
					return DependencyBuilder.this;
				}

				/**
				 * @return Pipeline stages mask
				 */
				private int stages() {
					final int mask = IntegerEnumeration.mask(stages);
					if(mask == 0) throw new IllegalArgumentException("No pipeline stage(s) specified for sub-pass dependency: subpass=" + index);
					return mask;
				}

				/**
				 * @return Access flags mask
				 */
				private int access() {
					return IntegerEnumeration.mask(access);
				}
			}

			private final Dependency src, dest;

			/**
			 * Constructor.
			 * @param src		Source index
			 * @param dest		Destination index
			 * @throws IllegalArgumentException if the source index is greater-than the destination
			 */
			private DependencyBuilder(int src, int dest) {
				if((src > dest) && (src != VK_SUBPASS_EXTERNAL) && (dest != VK_SUBPASS_EXTERNAL)) {
					throw new IllegalArgumentException(String.format("Source subpass %d cannot be greater-than destination %d", src, dest));
				}
				this.src = new Dependency(src);
				this.dest = new Dependency(dest);
			}

			/**
			 * @return Source dependency
			 */
			public Dependency source() {
				return src;
			}

			/**
			 * @return Destination dependency
			 */
			public Dependency destination() {
				return dest;
			}

			/**
			 * Populates the dependency descriptor.
			 */
			void populate(VkSubpassDependency dep) {
				// Populate source
				dep.srcSubpass = src.index;
				dep.srcStageMask = src.stages();
				dep.srcAccessMask = src.access();

				// Populate destination
				dep.dstSubpass = dest.index;
				dep.dstStageMask = dest.stages();
				dep.dstAccessMask = dest.access();
			}
			// TODO - dependency flags

			/**
			 * Constructs this dependency.
			 */
			public Builder build() {
				// TODO - src = dest = external invalid?
				dependencies.add(this);
				return Builder.this;
			}
		}
	}
}
