package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.Subpass.*;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> is comprised of a number of sub-passes.
 * @see Subpass
 * @author Sarge
 */
public class RenderPass extends AbstractVulkanObject {
	/**
	 * Creates a render pass.
	 * @param dev			Logical device
	 * @param subpasses		Sub-passes comprising this render pass
	 * @return New render pass
	 * @throws IllegalArgumentException if the list of sub-passes is empty or for a dependency that refers to a missing sub-pass
	 * @see Group
	 */
	public static RenderPass create(DeviceContext dev, List<Subpass> subpasses) {
		// Validate
		if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one subpass must be specified");
		if(subpasses.contains(Subpass.EXTERNAL) || subpasses.contains(Subpass.SELF)) throw new IllegalArgumentException("Cannot explicitly use a special case subpass");

		// Create sub-pass group helper
		final Group group = new Group(subpasses);

		// Create render pass descriptor
		final var info = new VkRenderPassCreateInfo();

		// Add attachments
		final List<Attachment> attachments = group.attachments();
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureHelper.pointer(attachments, VkAttachmentDescription::new, Attachment::populate);

		// Add sub-passes
		info.subpassCount = subpasses.size();
		info.pSubpasses = StructureHelper.pointer(subpasses, VkSubpassDescription::new, group::populate);

		// Populate dependencies
		final var dependencies = group.dependencies();
		info.dependencyCount = dependencies.size();
		info.pDependencies = StructureHelper.pointer(dependencies, VkSubpassDependency::new, group::populate);

		// Allocate render pass
		final VulkanLibrary lib = dev.library();
		final PointerByReference pass = dev.factory().pointer();
		check(lib.vkCreateRenderPass(dev, info, null, pass));

		// Create render pass
		return new RenderPass(pass.getValue(), dev, attachments);
	}

	private final List<Attachment> attachments;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param dev				Logical device
	 * @param attachments		Attachments
	 */
	private RenderPass(Pointer handle, DeviceContext dev, List<Attachment> attachments) {
		super(handle, dev);
		this.attachments = attachments;
	}

	/**
	 * @return Attachments used in this render-pass
	 */
	public List<Attachment> attachments() {
		return attachments;
	}

	@Override
	protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
		return lib::vkDestroyRenderPass;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(attachments)
				.build();
	}

	/**
	 * Helper class used to generate Vulkan descriptors for the render pass.
	 * <p>
	 * The render pass is represented as an object graph of the various sub-pass domain objects, whereas the resultant Vulkan descriptors use indices to represent the object relationships.
	 * This helper attempts to mitigate this by encapsulating index mapping whilst co-locating the population functions with the relevant type where possible.
	 * <p>
	 */
	private static class Group {
		/**
		 * Index of the implicit sub-pass before or after the render pass.
		 */
		private static final int VK_SUBPASS_EXTERNAL = (~0);

		private final List<Subpass> subpasses;
		private final List<Reference> references;

		/**
		 * Constructor.
		 * @param subpasses Sub-passes in this group
		 */
		private Group(List<Subpass> subpasses) {
			this.subpasses = subpasses;
			this.references = references(subpasses);
		}

		/**
		 * @return Unique attachment references in this group
		 */
		private static List<Reference> references(List<Subpass> subpasses) {
			return subpasses
					.stream()
					.flatMap(Subpass::attachments)
					.distinct()
					.toList();
		}

		/**
		 * @return Total attachments in this group
		 */
		public List<Attachment> attachments() {
			return references
					.stream()
					.map(Reference::attachment)
					.toList();
		}

		/**
		 * Populates a sub-pass descriptor.
		 * @param subpass			Sub-pass
		 * @param descriptor		Descriptor to populate
		 */
		public void populate(Subpass subpass, VkSubpassDescription descriptor) {
			// Init descriptor
			descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

			// Populate colour attachments
			final List<Reference> colour = subpass.colour();
			descriptor.colorAttachmentCount = colour.size();
			descriptor.pColorAttachments = StructureHelper.pointer(colour, VkAttachmentReference::new, this::populate);

			// Populate depth attachment
			descriptor.pDepthStencilAttachment = subpass.depth().map(this::depth).orElse(null);
		}

		/**
		 * Populates the descriptor for an attachment reference.
		 */
		private void populate(Reference ref, VkAttachmentReference descriptor) {
			final int index = references.indexOf(ref);
			ref.populate(index, descriptor);
		}

		/**
		 * Creates and populates a descriptor for the depth-stencil attachment.
		 */
		private VkAttachmentReference depth(Reference ref) {
			final var descriptor = new VkAttachmentReference();
			populate(ref, descriptor);
			return descriptor;
		}

		/**
		 * @return Sub-pass dependencies zipped with the <b>destination</b> sub-pass
		 */
		public List<Pair<Subpass, Dependency>> dependencies() {
			return subpasses
					.stream()
					.flatMap(subpass -> subpass.dependencies().map(e -> Pair.of(subpass, e)))
					.toList();
		}

		/**
		 * Populates the descriptor for a sub-pass dependency.
		 * @throws IllegalArgumentException if the sub-pass is not present
		 */
		public void populate(Pair<Subpass, Dependency> entry, VkSubpassDependency descriptor) {
			// Lookup index of this sub-pass
			final Subpass subpass = entry.getLeft();
			final Dependency dependency = entry.getRight();
			final int dest = subpasses.indexOf(subpass);
			assert dest >= 0;

			// Determine index of the source sub-pass
			final int src = index(dependency.subpass(), dest);

			// Populate descriptor
			dependency.populate(src, dest, descriptor);
		}

		/**
		 * Determines the index of the source sub-pass.
		 * @param src		Source sub-pass
		 * @param dest		Index of the destination (i.e. this) sub-pass (for the {@link Subpass#SELF} case)
		 * @return Source sub-pass index
		 * @throws IllegalArgumentException if the sub-pass is not present
		 */
		private int index(Subpass src, int dest) {
			if(src == Subpass.EXTERNAL) {
				return VK_SUBPASS_EXTERNAL;
			}
			else
			if(src == Subpass.SELF) {
				return dest;
			}
			else {
				final int index = subpasses.indexOf(src);
				if(index == -1) throw new IllegalArgumentException("Invalid subpass for this render pass: " + src);
				return index;
			}
		}
	}

	/**
	 * Render pass API.
	 */
	interface Library {
		/**
		 * Creates a render pass.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pRenderPass		Returned render pass handle
		 * @return Result code
		 */
		int vkCreateRenderPass(DeviceContext device, VkRenderPassCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pRenderPass);

		/**
		 * Destroys a render pass.
		 * @param device			Logical device
		 * @param renderPass		Render pass
		 * @param pAllocator		Allocator
		 */
		void vkDestroyRenderPass(DeviceContext device, RenderPass renderPass, Pointer pAllocator);

		/**
		 * Begins a render pass.
		 * @param commandBuffer			Command buffer
		 * @param pRenderPassBegin		Descriptor
		 * @param contents				Sub-pass contents
		 */
		void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);

		/**
		 * Ends a render pass.
		 * @param commandBuffer Command buffer
		 */
		void vkCmdEndRenderPass(Buffer commandBuffer);

		/**
		 * Starts the next sub-pass.
		 * @param commandBuffer			Command buffer
		 * @param contents				Sub-pass contents
		 */
		void vkCmdNextSubpass(Buffer commandBuffer, VkSubpassContents contents);
	}
}
