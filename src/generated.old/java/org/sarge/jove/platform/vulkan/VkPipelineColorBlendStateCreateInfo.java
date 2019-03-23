package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"logicOpEnable",
	"logicOp",
	"attachmentCount",
	"pAttachments",
	"blendConstants"
})
public class VkPipelineColorBlendStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineColorBlendStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineColorBlendStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public boolean logicOpEnable;
	public int logicOp = VkLogicOp.VK_LOGIC_OP_COPY.value();
	public int attachmentCount;
	public Pointer pAttachments;
	public float[] blendConstants = new float[4];

	/**
	 * Builder for the colour blend descriptor.
	 */
	public static class Builder {
		private boolean enable;
		private VkLogicOp op = VkLogicOp.VK_LOGIC_OP_COPY;
		private final List<VkPipelineColorBlendAttachmentState> attachments = new ArrayList<>();
		private final float[] constants = new float[4];

		/**
		 * Sets whether to use bitwise colour blending.
		 * @param enable Whether to enable bitwise blending
		 */
		public Builder enable(boolean enable) {
			this.enable = enable;
			return this;
		}

		// TODO
		// - op
		// - constants
		// - op/constants only valid if enabled

		/**
		 * Adds a colour blend attachment.
		 * @param attachment Attachment
		 */
		public Builder attach(VkPipelineColorBlendAttachmentState attachment) {
			attachments.add(notNull(attachment));
			return this;
		}

		/**
		 * Constructs this colour blend descriptor.
		 * @return New colour blend descriptor
		 */
		public VkPipelineColorBlendStateCreateInfo build() {
			final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();
			info.logicOpEnable = enable;
			info.logicOp = op.value();
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureHelper.allocate(attachments.toArray(VkPipelineColorBlendAttachmentState[]::new));
			System.arraycopy(constants, 0, info.blendConstants, 0, constants.length);
			return info;
		}
	}
}
