package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * Colour blending pipeline stage.
 * @author Sarge
 */
public class ColourBlendStage {
	private final List<ColourBlendAttachment> attachments = new ArrayList<>();
	private boolean logicOpEnable = false;
	private VkLogicOp logicOp = VkLogicOp.COPY;
	private final float[] blendConstants = new float[4];

	public ColourBlendStage() {
		Arrays.fill(blendConstants, 1);
	}

	/**
	 * Enables global bitwise colour blending.
	 */
	public ColourBlendStage enable() {
		this.logicOpEnable = true;
		return this;
	}

	/**
	 * Sets the global colour blending operation.
	 * @param op Colour-blending operation
	 */
	public ColourBlendStage operation(VkLogicOp op) {
		this.logicOp = op;
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IndexOutOfBoundsException if the given array does not contain <b>four</b> values
	 */
	public ColourBlendStage constants(float[] constants) {
		copy(constants, this.blendConstants);
		return this;
	}

	private static void copy(float[] src, float[] dest) {
		System.arraycopy(src, 0, dest, 0, dest.length);
	}

	/**
	 * Adds the colour blending descriptor for an attachment.
	 * @param attachment colour blending attachment descriptor
	 * TODO - injects? doc has to equal frame buffers?
	 */
	public ColourBlendStage add(ColourBlendAttachment attachment) {
		attachments.add(attachment);
		return this;
	}

	/**
	 * @return Colour blending descriptor
	 */
	VkPipelineColorBlendStateCreateInfo descriptor() {
		// Init blending descriptor
		final var info = new VkPipelineColorBlendStateCreateInfo();
		info.flags = 0;
		info.logicOpEnable = logicOpEnable;
		info.logicOp = logicOp;
		info.blendConstants = blendConstants;

		// Populate attachment descriptors or inject a single default entry
		// TODO - leave this up to the app?
		if(attachments.isEmpty()) {
			final var disabled = new VkPipelineColorBlendAttachmentState();
			disabled.blendEnable = false;
	   		info.attachmentCount = 1;
	   		info.pAttachments = new VkPipelineColorBlendAttachmentState[]{disabled};
		}
		else {
       		info.attachmentCount = attachments.size();
       		info.pAttachments = attachments.stream().map(ColourBlendAttachment::populate).toArray(VkPipelineColorBlendAttachmentState[]::new);
		}

		return info;
	}

//	/**
//	 * Creates a command to dynamically set the blend constants.
//	 * @param constants Blend constants
//	 * @return Dynamic blend constants command
//	 * @throws IndexOutOfBoundsException if the given array does not contain <b>four</b> values
//	 */
//	public Command setDynamicBlendConstants(float[] constants) {
//		final float[] copy = new float[info.blendConstants.length];
//		copy(constants, copy);
//		return (lib, buffer) -> lib.vkCmdSetBlendConstants(buffer, copy);
//	}

	/**
	 * Descriptor for the blending function of a colour or alpha channel.
	 */
	public record BlendOperation(VkBlendFactor src, VkBlendOp blend, VkBlendFactor dest) {
		/**
		 * Constructor.
		 * @param src			Source factor
		 * @param blend			Blending function
		 * @param dest			Destination factor
		 */
		public BlendOperation {
			requireNonNull(src);
			requireNonNull(blend);
			requireNonNull(dest);
		}

		/**
		 * Creates the common blending operation for the colour channel.
		 * @return Default colour channel blending operation
		 */
		public static BlendOperation colour() {
			return new BlendOperation(VkBlendFactor.SRC_ALPHA, VkBlendOp.ADD, VkBlendFactor.ONE_MINUS_SRC_ALPHA);
		}
		// TODO - could make this more smarty pants? the ONE_MINUS is always the next factor (one exception for saturate?).

		/**
		 * Creates the common blending operation for the alpha channel.
		 * @return Default alpha channel blending operation
		 */
		public static BlendOperation alpha() {
			return new BlendOperation(VkBlendFactor.ONE, VkBlendOp.ADD, VkBlendFactor.ZERO);
		}
	}

	/**
	 * Colour blending descriptor for an attachment.
	 */
	public record ColourBlendAttachment(BlendOperation colour, BlendOperation alpha, EnumMask<VkColorComponent> mask) {
		/**
		 *
		 */
		public static final EnumMask<VkColorComponent> DEFAULT_WRITE_MASK = new EnumMask<>(EnumSet.allOf(VkColorComponent.class));

		/**
		 * Constructor.
		 * @param colour		Colour channel blending operation
		 * @param alpha			Alpha blending operation
		 * @param mask			Colour component mask
		 */
		public ColourBlendAttachment {
			requireNonNull(colour);
			requireNonNull(alpha);
			requireNonNull(mask);
		}

		/**
		 * @return Descriptor
		 */
		VkPipelineColorBlendAttachmentState populate() {
			final var info = new VkPipelineColorBlendAttachmentState();
			info.blendEnable = true;
			info.srcColorBlendFactor = colour.src;
			info.dstColorBlendFactor = colour.dest;
			info.colorBlendOp = colour.blend;
			info.srcAlphaBlendFactor = alpha.src;
			info.dstAlphaBlendFactor = alpha.dest;
			info.alphaBlendOp = alpha.blend;
			info.colorWriteMask = mask;
			return info;
		}

		/**
		 * Builder for a colour blend attachment.
		 */
		public static class Builder {
			private BlendOperation colour = BlendOperation.colour();
			private BlendOperation alpha = BlendOperation.alpha();
			private EnumMask<VkColorComponent> mask = DEFAULT_WRITE_MASK;

			/**
			 * Sets the blending operation for the colour channel.
			 * @param colour Colour blending operation
			 * @see BlendOperation#colour()
			 */
			public Builder colour(BlendOperation colour) {
				this.colour = colour;
				return this;
			}

			/**
			 * Sets the blending operation for the alpha channel.
			 * @param alpha Alpha blending operation
			 * @see BlendOperation#alpha()
			 */
			public Builder alpha(BlendOperation alpha) {
				this.alpha = alpha;
				return this;
			}

			/**
			 * Sets the colour component mask.
			 * @param mask Colour component mask
			 * @see #mask(String)
			 */
			public Builder mask(Set<VkColorComponent> mask) {
				this.mask = new EnumMask<>(mask);
				return this;
			}

			/**
			 * Sets the colour write mask specified by the given string, e.g. {@code RGBA}
			 * @param mask Colour component mask represented as a string
			 * @throws IllegalArgumentException if {@link #mask} contains an unknown colour component character
			 * @see #mask(Set)
			 * @see VkColorComponent
			 */
			public Builder mask(String mask) {
				final Set<VkColorComponent> set = mask
						.toUpperCase()
						.chars()
						.mapToObj(Character::toString)
						.map(VkColorComponent::valueOf)
						.collect(toSet());

				return mask(set);
			}

			/**
			 * Constructs this colour blending attachment.
			 * @return New colour blending attachment
			 */
			public ColourBlendAttachment build() {
				return new ColourBlendAttachment(colour, alpha, mask);
			}
		}
	}
}
