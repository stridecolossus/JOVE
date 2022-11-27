package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.util.*;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendStageBuilder extends AbstractStageBuilder<VkPipelineColorBlendStateCreateInfo> {
	private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
	private final List<AttachmentBuilder> attachments = new ArrayList<>();

	public ColourBlendStageBuilder() {
		info.flags = 0;			// Reserved
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
		return builder;
	}

	/**
	 * Enables colour blending.
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
		info.logicOp = notNull(op);
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
		// Init default attachment if none specified
		if(attachments.isEmpty()) {
			final var builder = new AttachmentBuilder();
			builder.build();
		}

		// Add attachment descriptors
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureCollector.pointer(attachments, new VkPipelineColorBlendAttachmentState(), AttachmentBuilder::populate);

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
	 * Nested builder for a colour-blend attachment.
	 */
	public class AttachmentBuilder {
		/**
		 * Blend operation builder.
		 */
		public class BlendOperationBuilder {
			private VkBlendFactor src = VkBlendFactor.ONE;
			private VkBlendFactor dest = VkBlendFactor.ZERO;
			private VkBlendOp blend = VkBlendOp.ADD;

			private BlendOperationBuilder() {
			}

			/**
			 * Sets the source colour blend factor.
			 * @param src Source colour blend factor
			 */
			public BlendOperationBuilder source(VkBlendFactor src) {
				this.src = notNull(src);
				return this;
			}

			/**
			 * Sets the destination colour blend factor.
			 * @param dest Destination colour blend factor
			 */
			public BlendOperationBuilder destination(VkBlendFactor dest) {
				this.dest = notNull(dest);
				return this;
			}

			/**
			 * Sets the colour blend operation.
			 * @param blend Colour blend operation
			 */
			public BlendOperationBuilder operation(VkBlendOp blend) {
				this.blend = notNull(blend);
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

		private boolean enabled;
		private List<VkColorComponent> mask = MASK;
		private final BlendOperationBuilder colour = new BlendOperationBuilder();
		private final BlendOperationBuilder alpha = new BlendOperationBuilder();

		private AttachmentBuilder() {
			attachments.add(this);
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
			info.colorWriteMask = BitMask.reduce(mask);
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
