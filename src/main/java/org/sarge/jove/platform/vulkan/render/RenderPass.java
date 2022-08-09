package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import javax.swing.GroupLayout.Group;

import org.apache.commons.lang3.builder.ToStringBuilder;
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

		// Enumerate overall set of attachment references
		final Reference[] references = subpasses
				.stream()
				.flatMap(Subpass::references)
				.distinct()
				.toArray(Reference[]::new);

		// Init attachment indices
		for(int n = 0; n < references.length; ++n) {
			references[n].index(n);
		}

		// Init sub-pass indices
		for(int n = 0; n < subpasses.size(); ++n) {
			final Subpass subpass = subpasses.get(n);
			subpass.index(n);
		}

		// Init render pass descriptor
		final var info = new VkRenderPassCreateInfo();

		// Add attachments
		final List<Attachment> attachments = Arrays.stream(references).map(Reference::attachment).toList();
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureHelper.pointer(attachments, VkAttachmentDescription::new, Attachment::populate);

		// Add sub-passes
		info.subpassCount = subpasses.size();
		info.pSubpasses = StructureHelper.pointer(subpasses, VkSubpassDescription::new, Subpass::populate);

		// Add dependencies
		final List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
		info.dependencyCount = dependencies.size();
		info.pDependencies = StructureHelper.pointer(dependencies, VkSubpassDependency::new, Dependency::populate);

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
		this.attachments = List.copyOf(attachments);
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
	}
}
