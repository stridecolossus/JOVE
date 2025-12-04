package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.EnumMask;

/**
 * The <i>colour blend stage</i> configures the colour blending fixed function of the pipeline.
 * <p>
 * Colour blending has two modes of operation:
 * <p>
 * By default colour blending is configured <i>per attachment</i> by specifying a {@link ColourBlendAttachment} for each swapchain attachment.
 * <p>
 * Alternatively <i>global bitwise blending</i> overrides this behaviour by configuring the {@link ColourBlendStage#operation(VkLogicOp)} logic operation for <b>all</b> attachments.
 * <p>
 * For convenience, if no attachments have been configured, this implementation injects a <b>single</b> {@link ColourBlendAttachment#DISABLED} attachment.
 * This disables both modes, in which case fragment colours are written to the frame buffer unmodified.
 * <p>
 * Note that in <b>all</b> cases a {@link ColourBlendAttachment} descriptor <b>must</b> be configured for each colour attachment.
 * Additionally the colour write {@link ColourBlendAttachment#mask()} <b>must</b> be configured to pass through the resultant colours, otherwise rendering is essentially disabled entirely.
 * <p>
 * @author Sarge
 */
public class ColourBlendStage {
	private VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
	private final List<ColourBlendAttachment> attachments = new ArrayList<>();

	public ColourBlendStage() {
		info.logicOpEnable = false;
		info.logicOp = VkLogicOp.CLEAR;
		info.blendConstants = new float[4];
		Arrays.fill(info.blendConstants, 1);
	}

	/**
	 * Enables the global colour blending operation.
	 * Note this essentially disables the configuration for colour attachments specified by {@link #add(ColourBlendAttachment)}.
	 * @param logicOp Blending operation
	 */
	public ColourBlendStage operation(VkLogicOp logicOp) {
		info.logicOpEnable = true;
		info.logicOp = requireNonNull(logicOp);
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IllegalArgumentException if {@link #constants} does not contains 4 values
	 */
	public ColourBlendStage constants(float[] constants) {
		if(constants.length != info.blendConstants.length) {
			throw new IllegalArgumentException();
		}
		System.arraycopy(constants, 0, info.blendConstants, 0, constants.length);
		return this;
	}

	/**
	 * Configures the blending operation for a colour attachment.
	 * @param attachment Attachment colour blending configuration
	 */
	public ColourBlendStage add(ColourBlendAttachment attachment) {
		attachments.add(attachment);
		return this;
	}

	/**
	 * @return Colour blending descriptor
	 */
	VkPipelineColorBlendStateCreateInfo descriptor() {
		// Init descriptor
		info.flags = 0;

		// Inject a single disabled entry if no attachments are configured
		if(attachments.isEmpty()) {
			attachments.add(ColourBlendAttachment.DISABLED);
		}

		// Add blending configuration for colour attachments
		info.attachmentCount = attachments.size();
       	info.pAttachments = attachments
       			.stream()
       			.map(ColourBlendAttachment::populate)
       			.toArray(VkPipelineColorBlendAttachmentState[]::new);

		return info;
	}

	/**
	 * A <i>blend operation</i> configures the blending function of the colour or alpha channels.
	 */
	public record BlendOperation(VkBlendFactor source, VkBlendOp operation, VkBlendFactor destination) {
		/**
		 * Constructor.
		 * @param source			Source factor
		 * @param operation			Blend operation
		 * @param destination		Destination factor
		 */
		public BlendOperation {
			requireNonNull(source);
			requireNonNull(operation);
			requireNonNull(destination);
		}

		/**
		 * Helper.
		 * Creates the common blending operation for the colour channel.
		 * @return Default colour channel operation
		 */
		public static BlendOperation colour() {
			return new BlendOperation(VkBlendFactor.SRC_ALPHA, VkBlendOp.ADD, VkBlendFactor.ONE_MINUS_SRC_ALPHA);
		}

		/**
		 * Helper.
		 * Creates the common blending operation for the alpha channel.
		 * @return Default alpha channel operation
		 */
		public static BlendOperation alpha() {
			return new BlendOperation(VkBlendFactor.ONE, VkBlendOp.ADD, VkBlendFactor.ZERO);
		}
	}

	/**
	 * A <i>colour blend attachment</i> configures the blending operations of each colour attachment.
	 */
	public record ColourBlendAttachment(boolean enabled, BlendOperation colour, BlendOperation alpha, Set<VkColorComponentFlags> mask) {
		/**
		 * Default colour write mask containing <b>all</b> channels.
		 */
		public static final Set<VkColorComponentFlags> DEFAULT_WRITE_MASK = Set.of(VkColorComponentFlags.values());

		/**
		 * Defines an attachment with colour blending disabled.
		 */
		public static final ColourBlendAttachment DISABLED = new Builder().enable(false).build();

		/**
		 * Constructor.
		 * @param colour		Colour channel blending operation
		 * @param alpha			Alpha blending operation
		 * @param mask			Colour component write mask
		 * @see #DEFAULT_WRITE_MASK
		 */
		public ColourBlendAttachment {
			requireNonNull(colour);
			requireNonNull(alpha);
			mask = Set.copyOf(mask);
		}

		/**
		 * Helper.
		 * Builds a colour write mask specified by the given string, e.g. {@code RGBA}.
		 * @param mask Colour component write mask
		 * @throws IllegalArgumentException if {@link #mask} contains an unknown component character
		 * @see VkColorComponent
		 */
		public static Set<VkColorComponentFlags> mask(String mask) {
			return mask
					.toUpperCase()
					.chars()
					.mapToObj(Character::toString)
					.map(VkColorComponentFlags::valueOf)
					.collect(toSet());
		}

		/**
		 * @return Descriptor
		 */
		private VkPipelineColorBlendAttachmentState populate() {
			final var info = new VkPipelineColorBlendAttachmentState();
			info.blendEnable = enabled;
			info.srcColorBlendFactor = colour.source;
			info.dstColorBlendFactor = colour.destination;
			info.colorBlendOp = colour.operation;
			info.srcAlphaBlendFactor = alpha.source;
			info.dstAlphaBlendFactor = alpha.destination;
			info.alphaBlendOp = alpha.operation;
			info.colorWriteMask = new EnumMask<>(mask);
			return info;
		}

		/**
		 * Builder for a colour blend attachment.
		 */
		public static class Builder {
			private boolean enable = true;
			private BlendOperation colour = BlendOperation.colour();
			private BlendOperation alpha = BlendOperation.alpha();
			private Set<VkColorComponentFlags> mask = DEFAULT_WRITE_MASK;

			/**
			 * Sets whether blending is enabled for this attachment.
			 * @param enable Whether enabled
			 */
			public Builder enable(boolean enable) {
				this.enable = enable;
				return this;
			}

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
			 * Sets the colour write mask.
			 * @param mask Colour write mask
			 * @see ColourBlendAttachment#mask(String)
			 * @see ColourBlendAttachment#DEFAULT_WRITE_MASK
			 */
			public Builder mask(Set<VkColorComponentFlags> mask) {
				this.mask = mask;
				return this;
			}

			/**
			 * Constructs this colour blending attachment.
			 * @return New colour blending attachment
			 */
			public ColourBlendAttachment build() {
				return new ColourBlendAttachment(enable, colour, alpha, mask);
			}
		}
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
}
