package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkBlendFactor;
import org.sarge.jove.platform.vulkan.VkBlendOp;
import org.sarge.jove.platform.vulkan.VkColorComponentFlag;
import org.sarge.jove.platform.vulkan.VkLogicOp;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendAttachmentState;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.sarge.jove.platform.vulkan.util.StructureCollector;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendStageBuilder extends AbstractPipelineBuilder<VkPipelineColorBlendStateCreateInfo> {
	private static final int DEFAULT_COLOUR_MASK = IntegerEnumeration.mask(VkColorComponentFlag.values());

	private final List<AttachmentBuilder> attachments = new ArrayList<>();
	private final float[] constants = new float[4];
	private VkLogicOp logic; // NULL indicates no global colour blending (see build method)

	/**
	 * Starts a new attachment.
	 * @return New colour-blend attachment builder
	 */
	public AttachmentBuilder attachment() {
		return new AttachmentBuilder();
	}

	/**
	 * Sets the global colour blending operation.
	 * @param logic Colour-blending operation
	 */
	public ColourBlendStageBuilder logic(VkLogicOp logic) {
		this.logic = notNull(logic);
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IllegalArgumentException if the given array does not contain <b>four</b> values
	 */
	public ColourBlendStageBuilder constants(float[] constants) {
		if(constants.length != this.constants.length) throw new IllegalArgumentException(String.format("Expected exactly %d blend constants", this.constants.length));
		System.arraycopy(constants, 0, this.constants, 0, constants.length);
		return this;
	}

	/**
	 * Constructs the colour-blend stage descriptor.
	 * @return New descriptor
	 */
	@Override
	protected VkPipelineColorBlendStateCreateInfo result() {
		// Create descriptor
		final var info = new VkPipelineColorBlendStateCreateInfo();

		// Init default attachment if none specified
		if(attachments.isEmpty()) {
			new AttachmentBuilder().build();
		}

		// Add attachment descriptors
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureCollector.toPointer(attachments, VkPipelineColorBlendAttachmentState::new, AttachmentBuilder::populate);

		// Init global colour blending settings
		if(logic == null) {
			info.logicOpEnable = VulkanBoolean.FALSE;
			info.logicOp = VkLogicOp.NO_OP;
		}
		else {
			info.logicOpEnable = VulkanBoolean.TRUE;
			info.logicOp = logic;
			info.blendConstants = constants;
		}

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
			private VkBlendOp op = VkBlendOp.ADD;

			private BlendOperationBuilder() {
			}

			/**
			 * Sets the source colour blend factor.
			 * @param src Source colour blend factor
			 */
			public AttachmentBuilder source(VkBlendFactor src) {
				this.src = notNull(src);
				return AttachmentBuilder.this;
			}

			/**
			 * Sets the destination colour blend factor.
			 * @param dest Destination colour blend factor
			 */
			public AttachmentBuilder destination(VkBlendFactor dest) {
				this.dest = notNull(dest);
				return AttachmentBuilder.this;
			}

			/**
			 * Sets the colour blend operation.
			 * @param op Colour blend operation
			 */
			public AttachmentBuilder operation(VkBlendOp op) {
				this.op = notNull(op);
				return AttachmentBuilder.this;
			}
		}

		private boolean enabled;
		private int mask = DEFAULT_COLOUR_MASK;
		private final BlendOperationBuilder colour = new BlendOperationBuilder();
		private final BlendOperationBuilder alpha = new BlendOperationBuilder();

		private AttachmentBuilder() {
			colour.source(VkBlendFactor.SRC_ALPHA);
			colour.destination(VkBlendFactor.ONE_MINUS_SRC_ALPHA);
			alpha.source(VkBlendFactor.ONE);
			alpha.destination(VkBlendFactor.ZERO);
			attachments.add(this);
		}

		/**
		 * Sets whether blending is enabled for the current attachment.
		 * @param enabled Whether blending is enabled
		 */
		public AttachmentBuilder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		/**
		 * Sets the colour write mask (default is {@code RGBA}).
		 * @param mask Colour write mask
		 * @throws IllegalArgumentException if the mask contains an invalid colour component character
		 */
		public AttachmentBuilder mask(String mask) {
			this.mask = mask
					.chars()
					.mapToObj(Character::toString)
					.map(VkColorComponentFlag::valueOf)
					.mapToInt(IntegerEnumeration::value)
					.reduce(0, IntegerEnumeration.MASK);

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
			info.blendEnable = VulkanBoolean.of(enabled);

			// Init colour blending operation
			info.srcColorBlendFactor = colour.src;
			info.dstColorBlendFactor = colour.dest;
			info.colorBlendOp = colour.op;

			// Init alpha blending operation
			info.srcAlphaBlendFactor = alpha.src;
			info.dstAlphaBlendFactor = alpha.dest;
			info.alphaBlendOp = alpha.op;

			// Init colour write mask
			info.colorWriteMask = mask;
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
