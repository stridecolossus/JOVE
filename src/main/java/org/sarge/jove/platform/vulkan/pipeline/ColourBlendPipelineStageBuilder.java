package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sarge.jove.platform.vulkan.VkBlendFactor;
import org.sarge.jove.platform.vulkan.VkBlendOp;
import org.sarge.jove.platform.vulkan.VkColorComponent;
import org.sarge.jove.platform.vulkan.VkLogicOp;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendAttachmentState;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineColorBlendStateCreateInfo> {
	private static final int DEFAULT_COLOUR_MASK = IntegerEnumeration.mask(VkColorComponent.values());

	private final List<AttachmentBuilder> attachments = new ArrayList<>();
	private final float[] constants = new float[4];
	private VkLogicOp op;

	ColourBlendPipelineStageBuilder(Builder parent) {
		super(parent);
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
	 * @param op Colour-blending operation
	 */
	public ColourBlendPipelineStageBuilder operation(VkLogicOp op) {
		this.op = notNull(op);
		return this;
	}

	/**
	 * Sets the global blending constants.
	 * @param constants Blending constants array
	 * @throws IllegalArgumentException if the given array does not contain <b>four</b> values
	 */
	public ColourBlendPipelineStageBuilder constants(float[] constants) {
		if(constants.length != this.constants.length) throw new IllegalArgumentException(String.format("Expected exactly %d blend constants", this.constants.length));
		System.arraycopy(constants, 0, this.constants, 0, constants.length);
		return this;
	}

	/**
	 * Constructs the colour-blend stage descriptor.
	 * @return New descriptor
	 */
	@Override
	VkPipelineColorBlendStateCreateInfo get() {
		// Create descriptor
		final var info = new VkPipelineColorBlendStateCreateInfo();

		// Init default attachment if none specified
		if(attachments.isEmpty()) {
			new AttachmentBuilder().build();
		}

		// Add attachment descriptors
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureHelper.first(attachments, VkPipelineColorBlendAttachmentState::new, AttachmentBuilder::populate);

		// Init global colour blending settings
		if(op == null) {
			info.logicOpEnable = VulkanBoolean.FALSE;
			info.logicOp = VkLogicOp.NO_OP;
		}
		else {
			info.logicOpEnable = VulkanBoolean.TRUE;
			info.logicOp = op;
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
			private VkBlendOp blend = VkBlendOp.ADD;

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
			 * @param blend Colour blend operation
			 */
			public AttachmentBuilder operation(VkBlendOp blend) {
				this.blend = notNull(blend);
				return AttachmentBuilder.this;
			}

			// TODO - build() to return to AttachmentBuilder.this? i.e. not really a builder?
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
			// Convert mask to enumeration
			final Collection<VkColorComponent> components = mask
					.chars()
					.mapToObj(Character::toString)
					.map(VkColorComponent::valueOf)
					.collect(toList());

			// Convert to bit-field mask
			this.mask = IntegerEnumeration.mask(components);

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
			info.colorBlendOp = colour.blend;

			// Init alpha blending operation
			info.srcAlphaBlendFactor = alpha.src;
			info.dstAlphaBlendFactor = alpha.dest;
			info.alphaBlendOp = alpha.blend;

			// Init colour write mask
			info.colorWriteMask = mask;
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