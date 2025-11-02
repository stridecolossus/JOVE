package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.CommandBuffer;
import org.sarge.jove.platform.vulkan.render.Subpass.*;
import org.sarge.lib.Utility;

/**
 * A <i>render pass</i> is comprised of a number of sub passes that render to the frame buffer.
 * @see Subpass
 * @author Sarge
 */
public final class RenderPass extends VulkanObject {
	private final List<Attachment> attachments;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param dev				Logical device
	 * @param attachments		Attachments
	 */
	RenderPass(Handle handle, LogicalDevice dev, List<Attachment> attachments) {
		super(handle, dev);
		this.attachments = List.copyOf(attachments);
	}

	/**
	 * @return Attachments used in this render pass
	 */
	public List<Attachment> attachments() {
		return attachments;
	}

	/**
	 * Creates a command to advance to the next subpass.
	 * @param contents Subpass contents
	 * @return Next subpass command
	 */
	public static Command next(VkSubpassContents contents) {
		requireNonNull(contents);
		return (lib, buffer) -> lib.vkCmdNextSubpass(buffer, contents);
	}

	/**
	 * Creates a command to advance to the next subpass using {@link VkSubpassContents#INLINE}.
	 * @return Next subpass command
	 * @see #next(VkSubpassContents)
	 */
	public static Command next() {
		return next(VkSubpassContents.INLINE);
	}

	/**
	 * Queries the render area granularity for this render pass.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>An optimal render pass should have offsets and dimensions that are multiples of the returned granularity</li>
	 * <li>Subpass dependencies are not affected by the render area and apply to the entire sub-resource of the attached frame buffer</li>
	 * </ul>
	 * @return Render area granularity
	 * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/html/vkspec.html#vkGetRenderAreaGranularity">vkGetRenderAreaGranularity</a>
	 */
	public VkExtent2D granularity() {
		final LogicalDevice dev = this.device();
		final VulkanLibrary vulkan = dev.vulkan();
		final var area = new VkExtent2D();
		vulkan.vkGetRenderAreaGranularity(dev, this, area);
		return area;
	}

	@Override
	protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
		return lib::vkDestroyRenderPass;
	}

	/**
	 * Creates a render pass.
	 * @param dev			Logical device
	 * @param subpasses		Subpass list
	 * @return New render pass
	 * @throws IllegalArgumentException if {@link #subpasses} is empty, contains duplicates, or does not contain a dependant subpass
	 */
	public static RenderPass create(LogicalDevice dev, List<Subpass> subpasses) {
		// Validate
		if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one subpass must be specified");
		if(!Utility.distinct(subpasses)) throw new IllegalArgumentException("Subpasses cannot be duplicated");

		// Enumerate attachment references across the sub-passes
		final List<Reference> references = subpasses
				.stream()
				.flatMap(Subpass::attachments)
				.toList();

		// Aggregate the attachments for this render pass
		final List<Attachment> attachments = references
				.stream()
				.map(Reference::attachment)
				.distinct()
				.toList();

		// Patch attachment indices
		for(Reference ref : references) {
			final int index = attachments.indexOf(ref.attachment());
			ref.init(index);
		}

		// Patch subpass indices
		int index = 0;
		for(Subpass subpass : subpasses) {
			subpass.init(index++);
		}

		// Init render pass descriptor
		final var info = new VkRenderPassCreateInfo();
		info.flags = 0;			// Reserved

		// Add attachments
		info.attachmentCount = attachments.size();
		info.pAttachments = null; // TODO StructureCollector.pointer(attachments, new VkAttachmentDescription(), Attachment::populate);

		// Add sub-passes
		info.subpassCount = subpasses.size();
		info.pSubpasses = null; // TODO StructureCollector.pointer(subpasses, new VkSubpassDescription(), Subpass::populate);

		// Add dependencies
		final List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
		info.dependencyCount = dependencies.size();
		info.pDependencies = dependencies.stream().map(Dependency::descriptor).toArray(VkSubpassDependency[]::new);

		// Allocate render pass
		final VulkanLibrary vulkan = dev.vulkan();
		final Pointer ref = new Pointer();
		vulkan.vkCreateRenderPass(dev, info, null, ref);

		// Create render pass
		return new RenderPass(ref.get(), dev, attachments);
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
		 * @return Result
		 */
		VkResult vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Handle pAllocator, Pointer pRenderPass);

		/**
		 * Destroys a render pass.
		 * @param device			Logical device
		 * @param renderPass		Render pass
		 * @param pAllocator		Allocator
		 */
		void vkDestroyRenderPass(LogicalDevice device, RenderPass renderPass, Handle pAllocator);

		/**
		 * Begins a render pass.
		 * @param commandBuffer			Command buffer
		 * @param pRenderPassBegin		Descriptor
		 * @param contents				Sub-pass contents
		 */
		void vkCmdBeginRenderPass(CommandBuffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);

		/**
		 * Ends a render pass.
		 * @param commandBuffer Command buffer
		 */
		void vkCmdEndRenderPass(CommandBuffer commandBuffer);

		/**
		 * Starts the next sub-pass.
		 * @param commandBuffer			Command buffer
		 * @param contents				Sub-pass contents
		 */
		void vkCmdNextSubpass(CommandBuffer commandBuffer, VkSubpassContents contents);

		/**
		 * Queries the render area granularity for a render pass.
		 * @param dev					Logical device
		 * @param renderPass			Render pass
		 * @param pGranularity			Returned render area granularity
		 */
		void vkGetRenderAreaGranularity(LogicalDevice dev, RenderPass renderPass, @Returned VkExtent2D pGranularity);

		/**
		 * Clears attachments in this render pass.
		 * @param commandBuffer			Command buffer
		 * @param attachmentCount		Number of attachments
		 * @param pAttachments			Attachments to clear
		 * @param rectCount				Number of clear regions
		 * @param pRects				Clear regions
		 */
		void vkCmdClearAttachments(CommandBuffer commandBuffer, int attachmentCount, VkClearAttachment[] pAttachments, int rectCount, VkClearRect[] pRects);

//		void vkCmdClearColorImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdClearDepthStencilImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdResolveImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageResolve pRegions);
	}
}
