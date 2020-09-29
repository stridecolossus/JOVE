package org.sarge.jove.platform.vulkan.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.platform.vulkan.VkPipelineColorBlendAttachmentState;
import org.sarge.jove.platform.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.util.StructureHelper;

/**
 * Builder for the colour-blend pipeline stage.
 * @author Sarge
 */
public class ColourBlendStageBuilder extends AbstractPipelineStageBuilder {
	/**
	 * Helper - Creates a new default colour-blend descriptor.
	 * @return New descriptor
	 */
	static VkPipelineColorBlendStateCreateInfo create() {
		return new ColourBlendStageBuilder().buildLocal();
	}

	private final List<VkPipelineColorBlendAttachmentState> attachments = new ArrayList<>();
	private VkPipelineColorBlendAttachmentState current;

	public ColourBlendStageBuilder() {
		attachment();
	}

	/**
	 * Starts a new attachment entry.
	 * Note that the builder starts with a default attachment.
	 */
	public ColourBlendStageBuilder attachment() {
		current = new VkPipelineColorBlendAttachmentState();
		attachments.add(current);
		return this;
	}

	/**
	 * Sets whether blending is enabled for the current attachment.
	 * @param enabled Whether blending is enabled
	 */
	public ColourBlendStageBuilder enabled(boolean enabled) {
		current.blendEnable = VulkanBoolean.of(enabled);
		return this;
	}

	// TODO - the rest!

	/**
	 * Constructs the colour-blend stage descriptor.
	 * @return New descriptor
	 */
	public VkPipelineColorBlendStateCreateInfo buildLocal() {
		// Create descriptor
		final var info = new VkPipelineColorBlendStateCreateInfo();

		// Add attachment descriptors
		assert !attachments.isEmpty();
		info.attachmentCount = attachments.size();
		info.pAttachments = StructureHelper.structures(attachments);

		// TODO
		// info.logicOp
		// info.logicOpEnable

		return info;
	}
}
