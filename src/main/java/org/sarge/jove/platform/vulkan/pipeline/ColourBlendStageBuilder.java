package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.platform.vulkan.VkBlendFactor;
import org.sarge.jove.platform.vulkan.VkBlendOp;
import org.sarge.jove.platform.vulkan.VkLogicOp;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendAttachmentState;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendStageBuilder extends AbstractPipelineBuilder<VkPipelineColorBlendStateCreateInfo> {
	private final List<VkPipelineColorBlendAttachmentState> attachments = new ArrayList<>();
	private VkLogicOp logic;
	private final float[] constants = new float[4];

	/**
	 * Constructor.
	 */
	ColourBlendStageBuilder() {
		Arrays.fill(constants, 0);
	}

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
		info.pAttachments = StructureHelper.structures(attachments);

		// Init global colour blending settings
		if(logic == null) {
			info.logicOpEnable = VulkanBoolean.FALSE;
			info.logicOp = VkLogicOp.VK_LOGIC_OP_NO_OP;
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
			private VkBlendOp op = VkBlendOp.VK_BLEND_OP_ADD;

			/**
			 * Constructor.
			 * @param src		Default source blend factor
			 * @param dest		Default destination blend factor
			 */
			private BlendOperationBuilder(VkBlendFactor src, VkBlendFactor dest) {
				this.src = src;
				this.dest = dest;
			}

			private BlendOperationBuilder() {
			}

			/**
			 * Sets the source colour blend factor.
			 * @param srcBlendFactor Source colour blend factor
			 */
			public AttachmentBuilder sourceBlendFactor(VkBlendFactor src) {
				this.src = notNull(src);
				return AttachmentBuilder.this;
			}

			/**
			 * Sets the destination colour blend factor.
			 * @param dstBlendFactor Destination colour blend factor
			 */
			public AttachmentBuilder destinationBlendFactor(VkBlendFactor dest) {
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
		private final BlendOperationBuilder colour = new BlendOperationBuilder(VkBlendFactor.VK_BLEND_FACTOR_SRC_ALPHA, VkBlendFactor.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
		private final BlendOperationBuilder alpha = new BlendOperationBuilder(VkBlendFactor.VK_BLEND_FACTOR_ONE, VkBlendFactor.VK_BLEND_FACTOR_ZERO);

		private AttachmentBuilder() {
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
		 * Constructs this colour-blend attachment.
		 * @return Colour-blend builder
		 */
		public ColourBlendStageBuilder build() {
			// Construct attachment descriptor
			final var info = new VkPipelineColorBlendAttachmentState();
			info.blendEnable = VulkanBoolean.of(enabled);

			// Init colour blending operation
			info.srcColorBlendFactor = colour.src;
			info.dstColorBlendFactor = colour.dest;
			info.colorBlendOp = colour.op;

			// Init alpha blending operation
			info.srcAlphaBlendFactor = alpha.src;
			info.dstAlphaBlendFactor = alpha.dest;
			info.alphaBlendOp = alpha.op;

			// Add attachment
			attachments.add(info);

			// Return to parent builder
			return ColourBlendStageBuilder.this;
		}
	}
}
