package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.groupingBy;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.Attachment.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>subpass</i> configures a stage of a {@link RenderPass}.
 * @author Sarge
 */
public record Subpass(Set<VkSubpassDescriptionFlags> flags, List<Reference> references) {
	/**
	 * Constructor.
	 * @param flags			Subpass flags
	 * @param references	Attachments used by this subpass
	 */
	public Subpass {
		flags = Set.copyOf(flags);
		references = List.copyOf(references);
	}

	/**
	 * Builds the description for this subpass.
	 * @param attachments Aggregated attachments for this render pass
	 * @return Subpass description
	 * @throws IllegalArgumentException for an unknown attachment
	 */
	VkSubpassDescription description(List<Attachment> aggregated) {
		// Determines the index of an attachment reference
		final var indexer = new Object() {
			VkAttachmentReference reference(Reference reference) {
				final int index = aggregated.indexOf(reference.attachment());
				return reference.descriptor(index);
			}
		};

		// Init subpass descriptor
		final var description = new VkSubpassDescription();
		description.flags = new EnumMask<>(flags);
		description.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

		// Order attachments by type
		final Map<AttachmentType, List<Reference>> attachments = references
				.stream()
				.collect(groupingBy(ref -> ref.attachment().type()));

		// Add colour attachments
		final List<Reference> colour = attachments.getOrDefault(AttachmentType.COLOUR, List.of());
		description.colorAttachmentCount = colour.size();
		description.pColorAttachments = colour
				.stream()
				.map(indexer::reference)
				.toArray(VkAttachmentReference[]::new);

		// Add optional depth-stencil attachment
		final List<Reference> depth = attachments.get(AttachmentType.DEPTH);
		if(depth != null) {
			description.pDepthStencilAttachment = indexer.reference(depth.getFirst());
		}

		return description;
	}
}
