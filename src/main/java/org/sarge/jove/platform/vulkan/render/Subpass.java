package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>subpass</i> is a transient specification for a stage of a {@link RenderPass}.
 * @author Sarge
 */
public record Subpass(List<AttachmentReference> colour, AttachmentReference depth, Set<VkSubpassDescriptionFlags> flags) {
	/**
	 * An <i>attachment reference</i> specifies an attachment used in this subpass.
	 */
	public record AttachmentReference(Attachment attachment, VkImageLayout layout) {
		/**
		 * Constructor.
		 * @param attachment		Attachment
		 * @param layout			Image layout used during this subpass
		 */
		public AttachmentReference {
			requireNonNull(attachment);
			requireNonNull(layout);
		}
	}

	/**
	 * Constructor.
	 * @param colour		Colour attachments
	 * @param depth			Optional depth-stencil attachment
	 * @param flags			Subpass flags
	 */
	public Subpass {
		colour = List.copyOf(colour);
		flags = Set.copyOf(flags);
	}

	/**
	 * @return Attachments used by this subpass
	 */
	public Stream<AttachmentReference> attachments() {
		if(depth == null) {
			return colour.stream();
		}
		else {
			return Stream.concat(colour.stream(), Stream.of(depth));
		}
	}

	/**
	 * Builds the descriptor for this subpass.
	 * @param attachments Aggregated attachments for this render pass
	 * @return Subpass descriptor
	 * @throws IllegalArgumentException for an unknown attachment
	 */
	VkSubpassDescription description(List<Attachment> attachments) {
		// Builder for attachment references
		final var builder = new Object() {
			VkAttachmentReference build(AttachmentReference reference) {
				final var descriptor = new VkAttachmentReference();
				descriptor.layout = reference.layout;
				descriptor.attachment = attachments.indexOf(reference.attachment);
				if(descriptor.attachment < 0) {
					throw new IllegalArgumentException("Attachment not present in render pass: " + reference);
				}
				return descriptor;
			}
		};

		// Init subpass descriptor
		final var description = new VkSubpassDescription();
		description.flags = new EnumMask<>(flags);
		description.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

		// Add colour attachments
		description.colorAttachmentCount = colour.size();
		description.pColorAttachments = colour
				.stream()
				.map(builder::build)
				.toArray(VkAttachmentReference[]::new);

		// Add optional depth-stencil attachment
		if(depth != null) {
			description.pDepthStencilAttachment = builder.build(depth);
		}

		return description;
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this;
	}

	/**
	 * Builder for a subpass.
	 */
	public static class Builder {
		private final Set<VkSubpassDescriptionFlags> flags = new HashSet<>();
		private final List<AttachmentReference> colour = new ArrayList<>();
		private AttachmentReference depth;

		/**
		 * Adds a subpass creation flag.
		 * @param flag Create flag
		 */
		public Builder flag(VkSubpassDescriptionFlags flag) {
			flags.add(flag);
			return this;
		}

		/**
		 * Adds a colour attachment reference.
		 * @param colour Colour attachment reference
		 */
		public Builder colour(AttachmentReference colour) {
			this.colour.add(colour);
			return this;
		}

		/**
		 * Helper.
		 * Adds a colour attachment with a {@link VkImageLayout#COLOR_ATTACHMENT_OPTIMAL} layout.
		 * @param colour Colour attachment
		 */
		public Builder colour(Attachment attachment) {
			return colour(new AttachmentReference(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
		}

		/**
		 * Sets the depth-stencil attachment.
		 * @param depth Depth-stencil attachment
		 */
		public Builder depth(AttachmentReference depth) {
			this.depth = depth;
			return this;
		}

		/**
		 * Constructs this subpass.
		 * @return Subpass
		 */
		public Subpass build() {
			return new Subpass(colour, depth, flags);
		}
	}
}
