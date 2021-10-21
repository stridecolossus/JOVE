package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkAttachmentDescription;
import org.sarge.jove.platform.vulkan.VkAttachmentReference;
import org.sarge.jove.platform.vulkan.VkPipelineBindPoint;
import org.sarge.jove.platform.vulkan.VkRenderPassCreateInfo;
import org.sarge.jove.platform.vulkan.VkSubpassDependency;
import org.sarge.jove.platform.vulkan.VkSubpassDescription;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.render.Subpass.Reference;
import org.sarge.jove.platform.vulkan.render.Subpass.SubpassDependency;
import org.sarge.jove.platform.vulkan.render.Subpass.SubpassDependency.Dependency;
import org.sarge.jove.util.StructureHelper;

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
	private static final int VK_SUBPASS_EXTERNAL = (~0);

	/**
	 * Creates a render-pass.
	 * @param dev			Logical device
	 * @param subpasses		Sub-passes
	 * @return New render-pass
	 * @throws IllegalArgumentException if the list of sub-passes is empty
	 * @throws IllegalArgumentException for a sub-pass dependency that refers to a sub-pass not present in the given list
	 */
	public static RenderPass create(DeviceContext dev, List<Subpass> subpasses) {
		// Build render-pass descriptor
		if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
		if(subpasses.contains(Subpass.EXTERNAL)) throw new IllegalArgumentException("Cannot explicitly use the EXTERNAL sub-pass");
		final Helper helper = new Helper(subpasses);
		final VkRenderPassCreateInfo info = helper.populate();

		// Allocate render pass
		final VulkanLibrary lib = dev.library();
		final PointerByReference pass = lib.factory().pointer();
		check(lib.vkCreateRenderPass(dev, info, null, pass));

		// Create render pass
		return new RenderPass(pass.getValue(), dev, helper.attachments);
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
	 * Helper for building the render-pass descriptors.
	 */
	private static class Helper {
		private final List<Subpass> subpasses;
		private final List<Attachment> attachments;

		private Helper(List<Subpass> subpasses) {
			this.subpasses = subpasses;
			this.attachments = attachments(subpasses);
		}

		/**
		 * Enumerates <b>all</b> the attachments used in this render-pass.
		 */
		private static List<Attachment> attachments(List<Subpass> subpasses) {
			return subpasses
					.stream()
					.flatMap(Subpass::attachments)
					.distinct()
					.collect(toList());
		}

		/**
		 * Builds the render-pass descriptor.
		 */
		private VkRenderPassCreateInfo populate() {
			// Create render pass descriptor
			final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

			// Add attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureHelper.first(attachments, VkAttachmentDescription::new, Attachment::populate);

			// Add sub-passes
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureHelper.first(subpasses, VkSubpassDescription::new, this::subpass);

			// Add dependencies
			final List<Entry<Subpass, SubpassDependency>> dependencies = subpasses.stream().flatMap(Helper::stream).collect(toList());
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureHelper.first(dependencies, VkSubpassDependency::new, this::dependency);

			return info;
		}

		/**
		 * Populates a sub-pass descriptor.
		 */
		private void subpass(Subpass subpass, VkSubpassDescription desc) {
			// Init descriptor
			desc.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

			// Populate colour attachments
			final List<Reference> colour = subpass.colour();
			desc.colorAttachmentCount = colour.size();
			desc.pColorAttachments = StructureHelper.first(colour, VkAttachmentReference::new, this::reference);

			// Populate depth attachment
			desc.pDepthStencilAttachment = subpass.depth().map(this::depth).orElse(null);
		}

		/**
		 * @return Depth-stencil descriptor
		 */
		private VkAttachmentReference depth(Reference depth) {
			final VkAttachmentReference ref = new VkAttachmentReference();
			reference(depth, ref);
			return ref;
		}

		/**
		 * Populates an attachment reference descriptor.
		 */
		private void reference(Reference ref, VkAttachmentReference desc) {
			desc.attachment = attachments.indexOf(ref.attachment());
			desc.layout = ref.layout();
		}

		/**
		 * @return Zipped stream of a sub-pass and its dependencies
		 */
		private static Stream<Entry<Subpass, SubpassDependency>> stream(Subpass subpass) {
			return subpass
					.dependencies()
					.stream()
					.map(e -> Map.entry(subpass, e));
		}

		/**
		 * Populates a dependency descriptor from the given transient entry.
		 */
		private void dependency(Entry<Subpass, SubpassDependency> entry, VkSubpassDependency desc) {
			// Determine dependant sub-pass
			final SubpassDependency dependency = entry.getValue();
			final Subpass subpass = dependency.subpass() == Subpass.SELF ? entry.getKey() : dependency.subpass();

			// Populate source properties
			final Dependency src = dependency.source();
			desc.srcSubpass = indexOf(subpass);
			desc.srcStageMask = IntegerEnumeration.mask(src.stages());
			desc.srcAccessMask = IntegerEnumeration.mask(src.access());

			// Populate destination properties
			final Dependency dest = dependency.destination();
			desc.dstSubpass = indexOf(entry.getKey());
			desc.dstStageMask = IntegerEnumeration.mask(dest.stages());
			desc.dstAccessMask = IntegerEnumeration.mask(dest.access());
		}

		/**
		 * Looks up a sub-pass index.
		 */
		private int indexOf(Subpass subpass) {
			if(subpass == Subpass.EXTERNAL) {
				return VK_SUBPASS_EXTERNAL;
			}

			final int index = subpasses.indexOf(subpass);
			if(index == -1) throw new IllegalArgumentException("Sub-pass is not a member of this render-pass: " + subpass);
			return index;
		}
	}
}
