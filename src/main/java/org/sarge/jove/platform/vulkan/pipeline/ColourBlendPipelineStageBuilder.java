package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineColorBlendStateCreateInfo> {
	private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
	private final List<AttachmentBuilder> attachments = new ArrayList<>();

	public ColourBlendPipelineStageBuilder() {
		info.logicOpEnable = false;
		info.logicOp = VkLogicOp.COPY;
		Arrays.fill(info.blendConstants, 1);
	}

	/**
	 * Starts a new attachment.
	 * @return New colour-blend attachment builder
	 */
	public AttachmentBuilder attachment() {
		return new AttachmentBuilder();
	}

	/**
	 * Enables colour blending.
	 * @param enabled Whether enabled (default is {@code false})
	 */
	public ColourBlendPipelineStageBuilder enable(boolean enabled) {
		info.logicOpEnable = enabled;
		return this;
	}

	/**
	 * Sets the global colour blending operation.
	 * @param op Colour-blending operation
	 */
	public ColourBlendPipelineStageBuilder operation(VkLogicOp op) {
		info.logicOp = notNull(op);
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IllegalArgumentException if the given array does not contain <b>four</b> values
	 */
	public ColourBlendPipelineStageBuilder constants(float[] constants) {
		if(constants.length != info.blendConstants.length) throw new IllegalArgumentException(String.format("Expected exactly %d blend constants", info.blendConstants.length));
		System.arraycopy(constants, 0, info.blendConstants, 0, constants.length);
		return this;
	}

	@Override
	VkPipelineColorBlendStateCreateInfo get() {
		// Init default attachment if none specified
		if(attachments.isEmpty()) {
			new AttachmentBuilder().build();
		}

		// Add attachment descriptors
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureCollector.pointer(attachments, new VkPipelineColorBlendAttachmentState(), AttachmentBuilder::populate);

		return info;
	}

	/**
	 * Nested builder for a colour-blend attachment.
	 */
	public class AttachmentBuilder {
		/**
		 * Blend operation builder.
		 */
		public class BlendOperationBuilder {
			private VkBlendFactor src;
			private VkBlendFactor dest;
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

		private boolean enabled = true;
		private List<VkColorComponent> mask = Arrays.asList(VkColorComponent.values());
		private final BlendOperationBuilder colour = new BlendOperationBuilder();
		private final BlendOperationBuilder alpha = new BlendOperationBuilder();

		private AttachmentBuilder() {
			colour.source(VkBlendFactor.SRC_ALPHA);
			colour.destination(VkBlendFactor.ONE_MINUS_SRC_ALPHA);
			alpha.source(VkBlendFactor.ONE);
			alpha.destination(VkBlendFactor.ONE);
			attachments.add(this);
		}

		/**
		 * Sets whether blending is enabled for the colour attachment (default is {@code true}).
		 * @param enabled Whether blending is enabled
		 */
		public AttachmentBuilder enable(boolean enabled) {
			this.enabled = enabled;
			return this;
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
			info.colorWriteMask = BitField.reduce(mask);
		}

		/**
		 * Constructs this colour-blend attachment.
		 * @return Colour-blend builder
		 */
		public ColourBlendPipelineStageBuilder build() {
			return ColourBlendPipelineStageBuilder.this;
		}
	}
}
