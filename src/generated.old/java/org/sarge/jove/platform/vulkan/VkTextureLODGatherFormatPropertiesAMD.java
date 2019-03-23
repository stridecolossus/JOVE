package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"supportsTextureGatherLODBiasAMD"
})
public class VkTextureLODGatherFormatPropertiesAMD extends Structure {
	public static class ByValue extends VkTextureLODGatherFormatPropertiesAMD implements Structure.ByValue { }
	public static class ByReference extends VkTextureLODGatherFormatPropertiesAMD implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_TEXTURE_LOD_GATHER_FORMAT_PROPERTIES_AMD.value();
	public Pointer pNext;
	public boolean supportsTextureGatherLODBiasAMD;
}
