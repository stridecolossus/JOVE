package org.sarge.jove.platform.vulkan;

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
	"attachmentCount",
	"pAttachments",
	"subpassCount",
	"pSubpasses",
	"dependencyCount",
	"pDependencies"
})
public class VkRenderPassCreateInfo extends Structure {
	public static class ByValue extends VkRenderPassCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkRenderPassCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int attachmentCount;
	public Pointer pAttachments;
	public int subpassCount;
	public Pointer pSubpasses;
	public int dependencyCount;
	public Pointer pDependencies;
}
