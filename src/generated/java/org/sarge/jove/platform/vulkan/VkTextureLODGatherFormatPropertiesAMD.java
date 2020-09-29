package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

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
	"supportsTextureGatherLODBiasAMD"
})
public class VkTextureLODGatherFormatPropertiesAMD extends VulkanStructure {
	public static class ByValue extends VkTextureLODGatherFormatPropertiesAMD implements Structure.ByValue { }
	public static class ByReference extends VkTextureLODGatherFormatPropertiesAMD implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_TEXTURE_LOD_GATHER_FORMAT_PROPERTIES_AMD;
	public Pointer pNext;
	public VulkanBoolean supportsTextureGatherLODBiasAMD;
}
