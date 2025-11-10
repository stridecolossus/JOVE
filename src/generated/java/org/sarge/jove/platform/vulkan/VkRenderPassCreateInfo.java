package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkRenderPassCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.RENDER_PASS_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public int attachmentCount;
	public VkAttachmentDescription[] pAttachments;
	public int subpassCount;
	public VkSubpassDescription[] pSubpasses;
	public int dependencyCount;
	public VkSubpassDependency[] pDependencies;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            JAVA_INT.withName("flags"),
	            JAVA_INT.withName("attachmentCount"),
	            POINTER.withName("pAttachments"),
	            JAVA_INT.withName("subpassCount"),
	            PADDING,
	            POINTER.withName("pSubpasses"),
	            JAVA_INT.withName("dependencyCount"),
	            PADDING,
	            POINTER.withName("pDependencies")
	    );
	}
}
