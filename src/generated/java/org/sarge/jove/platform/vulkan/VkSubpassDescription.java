package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubpassDescription implements NativeStructure {
	public EnumMask<VkSubpassDescriptionFlag> flags;
	public VkPipelineBindPoint pipelineBindPoint;
	public int inputAttachmentCount;
	public Handle[] pInputAttachments;
	public int colorAttachmentCount;
	public VkAttachmentReference[] pColorAttachments;
	public Handle[] pResolveAttachments;
	public VkAttachmentReference pDepthStencilAttachment;
	public int preserveAttachmentCount;
	public Handle[] pPreserveAttachments;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("flags"),
	            JAVA_INT.withName("pipelineBindPoint"),
	            JAVA_INT.withName("inputAttachmentCount"),
	            PADDING,
	            POINTER.withName("pInputAttachments"),
	            JAVA_INT.withName("colorAttachmentCount"),
	            PADDING,
	            POINTER.withName("pColorAttachments"),
	            POINTER.withName("pResolveAttachments"),
	            POINTER.withName("pDepthStencilAttachment"),
	            JAVA_INT.withName("preserveAttachmentCount"),
	            PADDING,
	            POINTER.withName("pPreserveAttachments")
	    );
	}
}
