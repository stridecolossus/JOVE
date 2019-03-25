package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"validationCache"
})
public class VkShaderModuleValidationCacheCreateInfoEXT extends VulkanStructure {
	public static class ByValue extends VkShaderModuleValidationCacheCreateInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkShaderModuleValidationCacheCreateInfoEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_VALIDATION_CACHE_CREATE_INFO_EXT;
	public Pointer pNext;
	public long validationCache;
}