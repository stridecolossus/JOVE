package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkAttachmentDescription implements NativeStructure {
	public EnumMask<VkAttachmentDescriptionFlag> flags;
	public VkFormat format;
	public VkSampleCount samples;
	public VkAttachmentLoadOp loadOp;
	public VkAttachmentStoreOp storeOp;
	public VkAttachmentLoadOp stencilLoadOp;
	public VkAttachmentStoreOp stencilStoreOp;
	public VkImageLayout initialLayout;
	public VkImageLayout finalLayout;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("flags"),
	            JAVA_INT.withName("format"),
	            JAVA_INT.withName("samples"),
	            JAVA_INT.withName("loadOp"),
	            JAVA_INT.withName("storeOp"),
	            JAVA_INT.withName("stencilLoadOp"),
	            JAVA_INT.withName("stencilStoreOp"),
	            JAVA_INT.withName("initialLayout"),
	            JAVA_INT.withName("finalLayout")
	    );
	}
}
