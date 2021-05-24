package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> specifies how attachments are managed during rendering.
 * TODO
 * @author Sarge
 */
public class RenderPass extends AbstractVulkanObject {
	/**
	 * Index of the implicit sub-pass before or after the render pass.
	 */
	public static final int VK_SUBPASS_EXTERNAL = (~0);

	/**
	 * End render pass command.
	 */
	public static final Command END_COMMAND = (api, buffer) -> api.vkCmdEndRenderPass(buffer);

	private final List<Attachment> attachments;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param dev				Logical device
	 * @param attachments		Attachments
	 */
	private RenderPass(Pointer handle, LogicalDevice dev, List<Attachment> attachments) {
		super(handle, dev);
		this.attachments = List.copyOf(attachments);
	}

	/**
	 * @return Number of attachments
	 */
	public List<Attachment> attachments() {
		return attachments;
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

		// Populate rendering area
		final var extents = buffer.extents();
		info.renderArea.extent.width = extents.width();
		info.renderArea.extent.height = extents.height();

		// Map attachments to clear values
		final Collection<ClearValue> clear = buffer
				.attachments()
				.stream()
				.map(View::clear)
				.filter(Predicate.not(ClearValue.NONE::equals))
				.collect(toList());

		// Init clear values
		info.clearValueCount = clear.size();
		info.pClearValues = StructureHelper.first(clear, VkClearValue::new, ClearValue::populate);

		// Create command
		return (lib, handle) -> lib.vkCmdBeginRenderPass(handle, info, VkSubpassContents.INLINE);
	}

	@Override
	protected Destructor destructor(VulkanLibrary lib) {
		return lib::vkDestroyRenderPass;
	}

	/**
	 * Builder for a render pass.
	 */
	public static class Builder {
		private final List<Attachment> attachments = new ArrayList<>();
		private final List<SubPassBuilder> subpasses = new ArrayList<>();
		private final List<DependencyBuilder> dependencies = new ArrayList<>();

		/**
		 * Starts a sub-pass.
		 * @return New sub-pass builder
		 */
		public SubPassBuilder subpass() {
			return new SubPassBuilder();
		}

		/**
		 * Starts a new sub-pass dependency.
		 * <p>
		 * The special case {@link RenderPass#VK_SUBPASS_EXTERNAL} index is used for the implicit sub-pass before or after the render pass.
		 * <p>
		 * @param src		Source index
		 * @param dest		Destination index
		 * @return New dependency builder
		 * @throws IllegalArgumentException if the source index is greater-than the destination
		 */
		public DependencyBuilder dependency(int src, int dest) {
			final var dep = new DependencyBuilder(src, dest);
			dependencies.add(dep);
			return dep;
		}

		/**
		 * Populates (actually clones) an attachment descriptor.
		 * @param attachment		Attachment
		 * @param desc 				Attachment descriptor
		 */
		private static void populate(Attachment attachment, VkAttachmentDescription desc) {
			final VkAttachmentDescription copy = attachment.descriptor();
			desc.format = copy.format;
			desc.samples = copy.samples;
			desc.loadOp = copy.loadOp;
			desc.storeOp = copy.storeOp;
			desc.stencilLoadOp = copy.stencilLoadOp;
			desc.stencilStoreOp = copy.stencilStoreOp;
			desc.initialLayout = copy.initialLayout;
			desc.finalLayout = copy.finalLayout;
		}

		/**
		 * Constructs this render pass.
		 * @param dev Logical device
		 * @return New render pass
		 */
		public RenderPass build(LogicalDevice dev) {
			// Validate
			if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
			assert !attachments.isEmpty();

			// Create render pass descriptor
			final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

			// Add attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureHelper.first(attachments, VkAttachmentDescription::new, Builder::populate);

			// Add sub-passes
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureHelper.first(subpasses, VkSubpassDescription::new, SubPassBuilder::populate);

			// Add dependencies
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureHelper.first(dependencies, VkSubpassDependency::new, DependencyBuilder::populate);

			// Allocate render pass
			final VulkanLibrary lib = dev.library();
			final PointerByReference pass = lib.factory().pointer();
			check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

			// Create render pass
			return new RenderPass(pass.getValue(), dev, attachments);
		}

		/**
		 * Builder for a sub-pass.
		 */
		public class SubPassBuilder {
			private final Map<Integer, VkImageLayout> colour = new HashMap<>();
			private Map.Entry<Integer, VkImageLayout> depth;

			/**
			 * Adds a colour attachment.
			 * @param index			Attachment index
			 * @param layout		Attachment layout
			 */
			public SubPassBuilder colour(Attachment attachment, VkImageLayout layout) {
				final int index = add(attachment);
				if(colour.containsKey(index)) throw new IllegalArgumentException("Duplicate colour attachment: " + attachment);
				colour.put(index, layout);
				return this;
			}

			/**
			 * Adds the depth-buffer attachment.
			 * @param index Attachment index
			 * @throws IllegalArgumentException if the depth buffer has already been configured
			 */
			public SubPassBuilder depth(Attachment depth, VkImageLayout layout) {
				if(this.depth != null) throw new IllegalArgumentException("Depth attachment already configured");
				final int index = add(depth);
				this.depth = Map.entry(index, layout);
				return this;
			}

			/**
			 * Adds an attachment to this sub-pass.
			 * @param attachment Attachment to add
			 * @return Attachment index
			 */
			private int add(Attachment attachment) {
				Check.notNull(attachment);
				if(!attachments.contains(attachment)) {
					attachments.add(attachment);
				}
				return attachments.indexOf(attachment);
			}

			/**
			 * Populates the descriptor for this sub-pass.
			 * @param desc Sub-pass descriptor
			 */
			void populate(VkSubpassDescription desc) {
				// Init descriptor
				desc.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

				// Populate colour attachments
				desc.colorAttachmentCount = colour.size();
				desc.pColorAttachments = StructureHelper.first(colour.entrySet(), VkAttachmentReference::new, Builder::reference);

				// Populate depth attachment
				if(depth != null) {
					final var ref = new VkAttachmentReference();
					reference(depth, ref);
					desc.pDepthStencilAttachment = ref;
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
		 * Populates an attachment reference descriptor.
		 * @param entry		Attachment entry
		 * @param ref		Attachment reference descriptor
		 */
		private static void reference(Map.Entry<Integer, VkImageLayout> entry, VkAttachmentReference ref) {
			ref.attachment = entry.getKey();
			ref.layout = entry.getValue();
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
				private final Set<VkPipelineStage> stages = new HashSet<>();
				private final Set<VkAccess> access = new HashSet<>();

				/**
				 * Constructor.
				 * @param index Sub-pass index
				 * @throws IllegalArgumentException for an invalid index
				 */
				private Dependency(int index) {
					if((index != VK_SUBPASS_EXTERNAL) && ((index < 0) || (index >= subpasses.size()))) {
						throw new IllegalArgumentException("Invalid sub-pass index: " + index);
					}
					this.index = index;
				}

				/**
				 * Adds a pipeline stage.
				 * @param stage Pipeline stage
				 */
				public Dependency stage(VkPipelineStage stage) {
					stages.add(notNull(stage));
					return this;
				}

				/**
				 * Adds an access flag.
				 * @param access Access flag
				 */
				public Dependency access(VkAccess access) {
					this.access.add(notNull(access));
					return this;
				}

				/**
				 * @return Pipeline stages mask
				 */
				private int stages() {
					check();
					return IntegerEnumeration.mask(stages);
				}

				/**
				 * @return Access flags mask
				 */
				private int access() {
					return IntegerEnumeration.mask(access);
				}

				/**
				 * Constructs this dependency.
				 */
				public DependencyBuilder build() {
					check();
					return DependencyBuilder.this;
				}

				private void check() {
					if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stage(s) specified for sub-pass dependency: subpass=" + index);
				}
			}

			private final Dependency src, dest;

			/**
			 * Constructor.
			 * @param src		Source index
			 * @param dest		Destination index
			 * @throws IllegalArgumentException if the source index is greater-than-or-equal to the destination
			 */
			private DependencyBuilder(int src, int dest) {
				if(dest == VK_SUBPASS_EXTERNAL) {
					if(src == VK_SUBPASS_EXTERNAL) throw new IllegalArgumentException("Invalid implicit indices");
				}
				else
				if(src >= dest) {
					throw new IllegalArgumentException(String.format("Invalid dependency indices: src=%d dest=%d", src, dest));
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
				src.check();
				dest.check();
				return Builder.this;
			}
		}
	}
}
