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
	"maxVertexAttribDivisor"
})
public class VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int maxVertexAttribDivisor;
}
