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
	"validationCache"
})
public class VkShaderModuleValidationCacheCreateInfoEXT extends Structure {
	public static class ByValue extends VkShaderModuleValidationCacheCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkShaderModuleValidationCacheCreateInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_VALIDATION_CACHE_CREATE_INFO_EXT.value();
	public Pointer pNext;
	public long validationCache;
}
