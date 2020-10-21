package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"geometryType",
	"geometry",
	"flags"
})
public class VkGeometryNV extends VulkanStructure {
	public static class ByValue extends VkGeometryNV implements Structure.ByValue { }
	public static class ByReference extends VkGeometryNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_GEOMETRY_NV;
	public Pointer pNext;
	public VkGeometryTypeNV geometryType;
	public VkGeometryDataNV geometry;
	public int flags;
}
