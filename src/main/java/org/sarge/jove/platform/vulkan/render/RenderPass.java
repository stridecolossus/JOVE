package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.Subpass.Dependency;
import org.sarge.jove.util.StructureCollector;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> is comprised of a number of sub passes that render to the frame buffer.
 * @see Subpass
 * @author Sarge
 */
public class RenderPass extends AbstractVulkanObject {
	private final List<Attachment> attachments;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param dev				Logical device
	 * @param attachments		Attachments
	 */
	RenderPass(Handle handle, DeviceContext dev, List<Attachment> attachments) {
		super(handle, dev);
		this.attachments = List.copyOf(attachments);
	}

	/**
	 * @return Attachments used in this render-pass
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
	 * <li>Subpass dependencies are not affected by the render area and apply to the entire sub-resource of the attached frame buffer</li>
	 * </ul>
	 * @return Render area granularity
	 * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/html/vkspec.html#vkGetRenderAreaGranularity">vkGetRenderAreaGranularity</a>
	 */
	public VkExtent2D granularity() {
		final DeviceContext dev = this.device();
		final Library lib = dev.library();
		final var area = new VkExtent2D();
		lib.vkGetRenderAreaGranularity(dev, this, area);
		return area;
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
	 * Builder for a render-pass.
	 * <p>
	 * Example:
	 * <pre>
	 * RenderPass pass = new RenderPass.Builder()
	 *     .subpass()
	 *         .colour(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
	 *         .depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
	 *         .build()
	 *     .build(dev);
	 * </pre>
	 */
	public static class Builder {
		private final List<Subpass> subpasses = new ArrayList<>();
		private final List<Attachment> attachments = new ArrayList<>();

		/**
		 * @return New subpass
		 */
		public Subpass subpass() {
			final Subpass subpass = new Subpass(this, subpasses.size());
			subpasses.add(subpass);
			return subpass;
		}

		/**
		 * Adds an attachment to this render pass (if not already present).
		 * @param attachment Attach
		 * @return Attachment index
		 */
		int add(Attachment attachment) {
			int index = attachments.indexOf(attachment);
			if(index == -1) {
				index = attachments.size();
				attachments.add(attachment);
			}
			return index;
		}

		/**
		 * Constructs this render pass.
		 * @return New render pass
		 * @throws IllegalArgumentException if the list of sub-passes is empty or a dependency refers to a missing subpass
		 */
		public RenderPass build(DeviceContext dev) {
			// Validate
			if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one subpass must be specified");
			subpasses.forEach(Subpass::verify);

			// Init render pass descriptor
			final var info = new VkRenderPassCreateInfo();

			// Add attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureCollector.pointer(attachments, new VkAttachmentDescription(), Attachment::populate);

			// Add sub-passes
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureCollector.pointer(subpasses, new VkSubpassDescription(), Subpass::populate);

			// Add dependencies
			final List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureCollector.pointer(dependencies, new VkSubpassDependency(), Dependency::populate);

			// Allocate render pass
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateRenderPass(dev, info, null, ref));

			// Create render pass
			return new RenderPass(new Handle(ref), dev, attachments);
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
		 * @param pRenderPass		Returned render pass
		 * @return Result
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

		/**
		 * Queries the render area granularity for a render pass.
		 * @param dev					Logical device
		 * @param renderPass			Render pass
		 * @param pGranularity			Returned render area granularity
		 */
		void vkGetRenderAreaGranularity(DeviceContext dev, RenderPass renderPass, VkExtent2D pGranularity);

		/**
		 * Clears attachments in this render pass.
		 * @param commandBuffer			Command buffer
		 * @param attachmentCount		Number of attachments
		 * @param pAttachments			Attachments to clear
		 * @param rectCount				Number of clear regions
		 * @param pRects				Clear regions
		 */
		void vkCmdClearAttachments(Buffer commandBuffer, int attachmentCount, VkClearAttachment pAttachments, int rectCount, VkClearRect pRects);

//		void vkCmdClearColorImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearColorValue pColor, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdClearDepthStencilImage(Buffer commandBuffer, Image image, VkImageLayout imageLayout, VkClearDepthStencilValue pDepthStencil, int rangeCount, VkImageSubresourceRange pRanges);
//		void vkCmdResolveImage(Buffer commandBuffer, Image srcImage, VkImageLayout srcImageLayout, Image dstImage, VkImageLayout dstImageLayout, int regionCount, VkImageResolve pRegions);
	}
}
