package org.sarge.jove.platform.vulkan.render;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>render pass</i> is comprised of a number of sub passes that render to a frame buffer.
 * @see Subpass
 * @author Sarge
 */
public class RenderPass extends VulkanObject {
	private final List<Attachment> attachments;
	private final Library library;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param device			Logical device
	 * @param attachments		Attachments used by this render pass
	 */
	RenderPass(Handle handle, LogicalDevice device, List<Attachment> attachments) {
		super(handle, device);
		this.attachments = List.copyOf(attachments);
		this.library = device.library();
	}

	/**
	 * @return Attachments used by this render pass
	 */
	public List<Attachment> attachments() {
		return attachments;
	}

	/**
	 * Queries the render area granularity for this render pass.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>An optimal render pass should have offsets and dimensions that are multiples of the returned granularity</li>
	 * <li>Subpass dependencies are not affected by the render area and apply to the entire subresource of the attached frame buffer</li>
	 * </ul>
	 * @return Render area granularity
	 * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/html/vkspec.html#vkGetRenderAreaGranularity">vkGetRenderAreaGranularity</a>
	 */
	public VkExtent2D granularity() {
		final var area = new VkExtent2D();
		library.vkGetRenderAreaGranularity(this.device(), this, area);
		return area;
	}

	@Override
	protected Destructor<RenderPass> destructor() {
		return library::vkDestroyRenderPass;
	}

	/**
	 * Builder for a render pass.
	 */
	public static class Builder {
		private final List<Subpass> subpasses = new ArrayList<>();
		private final List<Dependency> dependencies = new ArrayList<>();

		/**
		 * Adds a subpass to this render pass.
		 * @param subpass Subpass to add
		 */
		public Builder add(Subpass subpass) {
			subpasses.add(subpass);
			return this;
		}

		/**
		 * Adds a subpass dependency.
		 * @param dependency Subpass dependency
		 */
		public Builder dependency(Dependency dependency) {
			dependencies.add(dependency);
			return this;
		}

		/**
		 * Constructs this render pass.
		 * @param device Logical device
		 * @return Render pass
		 * @throws IllegalArgumentException if the render pass is empty
		 * @throws IllegalArgumentException for an unknown attachment reference
		 * @throws IllegalArgumentException for an invalid subpass dependency
		 */
		public RenderPass build(LogicalDevice device) {
			// Validate
			if(subpasses.isEmpty()) {
				throw new IllegalArgumentException("At least one subpass must be specified");
			}

			// Aggregate attachments used across the subpasses
			final List<Attachment> attachments = subpasses
					.stream()
					.map(Subpass::references)
					.flatMap(List::stream)
					.map(Attachment.Reference::attachment)
					.distinct()
					.toList();

			// Build render pass descriptor
			final VkRenderPassCreateInfo info = descriptor(attachments);

			// Allocate render pass
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateRenderPass(device, info, null, pointer);

			// Create render pass
			return new RenderPass(pointer.handle(), device, attachments);
		}

		/**
		 * @param attachments Aggregated attachments
		 * @return Render pass descriptor
		 */
		private VkRenderPassCreateInfo descriptor(List<Attachment> attachments) {
			// Init descriptor
			final var info = new VkRenderPassCreateInfo();
			info.sType = VkStructureType.RENDER_PASS_CREATE_INFO;
			info.flags = new EnumMask<>();

			// Populate aggregated attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = attachments
					.stream()
					.map(Attachment::description)
					.map(AttachmentDescription::populate)
					.toArray(VkAttachmentDescription[]::new);

			// Populate subpasses
			info.subpassCount = subpasses.size();
			info.pSubpasses = subpasses
					.stream()
					.map(subpass -> subpass.description(attachments))
					.toArray(VkSubpassDescription[]::new);

			// Populate dependencies
			info.dependencyCount = dependencies.size();
			info.pDependencies = dependencies
					.stream()
					.map(dependency -> dependency.descriptor(subpasses))
					.toArray(VkSubpassDependency[]::new);

			return info;
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
		 * Starts the next sub-pass.
		 * @param commandBuffer			Command buffer
		 * @param contents				Sub-pass contents
		 */
		void vkCmdNextSubpass(Buffer commandBuffer, VkSubpassContents contents);

		/**
		 * Queries the render area granularity for a render pass.
		 * @param dev					Logical device
		 * @param renderPass			Render pass
		 * @param pGranularity			Returned render area granularity
		 */
		void vkGetRenderAreaGranularity(LogicalDevice dev, RenderPass renderPass, @Updated VkExtent2D pGranularity);

		/**
		 * Clears attachments in this render pass.
		 * @param commandBuffer			Command buffer
		 * @param attachmentCount		Number of attachments
		 * @param pAttachments			Attachments to clear
		 * @param rectCount				Number of clear regions
		 * @param pRects				Clear regions
		 */
		void vkCmdClearAttachments(Buffer commandBuffer, int attachmentCount, VkClearAttachment[] pAttachments, int rectCount, VkClearRect[] pRects);

//		void vkCmdClearColorImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdClearDepthStencilImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdResolveImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageResolve pRegions);
	}

//	/**
//	 * Creates a command to advance to the next subpass.
//	 * @param contents Subpass contents
//	 * @return Next subpass command
//	 */
//	public static Command next(VkSubpassContents contents, Library library) {
//		requireNonNull(contents);
//		return buffer -> library.vkCmdNextSubpass(buffer, contents);
//	}
//
//	/**
//	 * Creates a command to advance to the next subpass using {@link VkSubpassContents#INLINE}.
//	 * @return Next subpass command
//	 * @see #next(VkSubpassContents)
//	 */
//	public static Command next() {
//		final Library library = this.device().library();
//		return next(VkSubpassContents.INLINE);
//	}
}
