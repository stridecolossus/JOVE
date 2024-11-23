package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.util.BitMask;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendStageBuilder extends AbstractStageBuilder<VkPipelineColorBlendStateCreateInfo> {
	private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
	private final List<AttachmentBuilder> attachments = new ArrayList<>();

	public ColourBlendStageBuilder() {
		info.flags = 0;			// Reserved
		info.logicOpEnable = false;
		info.logicOp = VkLogicOp.COPY;
		Arrays.fill(info.blendConstants, 1);
	}

	/**
	 * Starts a new attachment.
	 * @return New colour-blend attachment builder
	 */
	public AttachmentBuilder attachment() {
		final var builder = new AttachmentBuilder();
		builder.enabled = true;
		attachments.add(builder);
		return builder;
	}

	/**
	 * Enables global bitwise colour blending.
	 */
	public ColourBlendStageBuilder enable() {
		info.logicOpEnable = true;
		return this;
	}

	/**
	 * Sets the global colour blending operation.
	 * @param op Colour-blending operation
	 */
	public ColourBlendStageBuilder operation(VkLogicOp op) {
		info.logicOp = requireNonNull(op);
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IndexOutOfBoundsException if the given array does not contain <b>four</b> values
	 */
	public ColourBlendStageBuilder constants(float[] constants) {
		copy(constants, info.blendConstants);
		return this;
	}

	private static void copy(float[] src, float[] dest) {
		System.arraycopy(src, 0, dest, 0, dest.length);
	}

	@Override
	VkPipelineColorBlendStateCreateInfo get() {
		if(attachments.isEmpty()) {
			attachments.add(new AttachmentBuilder());
		}

		info.attachmentCount = attachments.size();
		info.pAttachments = null; // TODO StructureCollector.pointer(attachments, new VkPipelineColorBlendAttachmentState(), AttachmentBuilder::populate);
		return info;
	}

	/**
	 * Creates a command to dynamically set the blend constants.
	 * @param constants Blend constants
	 * @return Dynamic blend constants command
	 * @throws IndexOutOfBoundsException if the given array does not contain <b>four</b> values
	 */
	public Command setDynamicBlendConstants(float[] constants) {
		final float[] copy = new float[info.blendConstants.length];
		copy(constants, copy);
		return (lib, buffer) -> lib.vkCmdSetBlendConstants(buffer, copy);
	}

	/**
	 * Builder for a colour-blend attachment.
	 */
	public class AttachmentBuilder {
		/**
		 * Blend operation builder.
		 */
		public class BlendOperationBuilder {
			private VkBlendFactor src;
			private VkBlendFactor dest;
			private VkBlendOp blend = VkBlendOp.ADD;

			/**
			 * Constructor.
			 * @param src		Source factor
			 * @param dest		Destination factor
			 */
			private BlendOperationBuilder(VkBlendFactor src, VkBlendFactor dest) {
				source(src);
				destination(dest);
			}

			/**
			 * Sets the source colour blend factor.
			 * @param src Source colour blend factor
			 */
			public BlendOperationBuilder source(VkBlendFactor src) {
				this.src = requireNonNull(src);
				return this;
			}

			/**
			 * Sets the destination colour blend factor.
			 * @param dest Destination colour blend factor
			 */
			public BlendOperationBuilder destination(VkBlendFactor dest) {
				this.dest = requireNonNull(dest);
				return this;
			}

			/**
			 * Sets the colour blend operation.
			 * @param blend Colour blend operation
			 */
			public BlendOperationBuilder operation(VkBlendOp blend) {
				this.blend = requireNonNull(blend);
				return this;
			}

			/**
			 * @return Parent attachment builder
			 */
			public AttachmentBuilder build() {
				return AttachmentBuilder.this;
			}
		}

		private static final List<VkColorComponent> MASK = Arrays.asList(VkColorComponent.values());

		private final BlendOperationBuilder colour = new BlendOperationBuilder(VkBlendFactor.SRC_ALPHA, VkBlendFactor.ONE_MINUS_SRC_ALPHA);
		private final BlendOperationBuilder alpha = new BlendOperationBuilder(VkBlendFactor.ONE, VkBlendFactor.ZERO);
		private List<VkColorComponent> mask = MASK;
		private boolean enabled;

		private AttachmentBuilder() {
		}

		/**
		 * Sets the colour write mask (default is {@code RGBA}).
		 * @param mask Colour write mask
		 * @throws IllegalArgumentException if the mask contains an invalid colour component character
		 * @see VkColorComponent
		 */
		public AttachmentBuilder mask(String mask) {
			this.mask = mask
					.chars()
					.mapToObj(Character::toString)
					.map(VkColorComponent::valueOf)
					.toList();
			return this;
		}

		/**
		 * @return Colour blend builder
		 */
		public BlendOperationBuilder colour() {
			return colour;
		}

		/**
		 * @return Alpha blend builder
		 */
		public BlendOperationBuilder alpha() {
			return alpha;
		}

		/**
		 * Populates the attachment descriptor.
		 */
		private void populate(VkPipelineColorBlendAttachmentState info) {
			// Init descriptor
			info.blendEnable = enabled;

			// Init colour blending operation
			info.srcColorBlendFactor = colour.src;
			info.dstColorBlendFactor = colour.dest;
			info.colorBlendOp = colour.blend;

			// Init alpha blending operation
			info.srcAlphaBlendFactor = alpha.src;
			info.dstAlphaBlendFactor = alpha.dest;
			info.alphaBlendOp = alpha.blend;

			// Init colour write mask
			info.colorWriteMask = new BitMask<>(mask);
		}

		/**
		 * Constructs this colour-blend attachment.
		 * @return Colour-blend builder
		 */
		public ColourBlendStageBuilder build() {
			return ColourBlendStageBuilder.this;
		}
	}
}
