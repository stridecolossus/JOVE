package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"maxVertexAttribDivisor"
})
public class VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_PROPERTIES_EXT;
	public Pointer pNext;
	public int maxVertexAttribDivisor;
}
