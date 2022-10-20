package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	public boolean residencyStandard2DBlockShape;
	public boolean residencyStandard2DMultisampleBlockShape;
	public boolean residencyStandard3DBlockShape;
	public boolean residencyAlignedMipSize;
	public boolean residencyNonResidentStrict;
}
