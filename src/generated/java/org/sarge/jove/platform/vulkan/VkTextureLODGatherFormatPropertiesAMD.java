package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"supportsTextureGatherLODBiasAMD"
})
public class VkTextureLODGatherFormatPropertiesAMD extends VulkanStructure {
	public VkStructureType sType = VkStructureType.TEXTURE_LOD_GATHER_FORMAT_PROPERTIES_AMD;
	public Pointer pNext;
	public VulkanBoolean supportsTextureGatherLODBiasAMD;
}
