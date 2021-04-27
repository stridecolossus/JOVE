package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"residencyStandard2DBlockShape",
	"residencyStandard2DMultisampleBlockShape",
	"residencyStandard3DBlockShape",
	"residencyAlignedMipSize",
	"residencyNonResidentStrict"
})
public class VkPhysicalDeviceSparseProperties extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceSparseProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSparseProperties implements Structure.ByReference { }
	
	public VulkanBoolean residencyStandard2DBlockShape;
	public VulkanBoolean residencyStandard2DMultisampleBlockShape;
	public VulkanBoolean residencyStandard3DBlockShape;
	public VulkanBoolean residencyAlignedMipSize;
	public VulkanBoolean residencyNonResidentStrict;
}
