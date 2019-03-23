package org.sarge.jove.platform.vulkan;

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
public class VkPhysicalDeviceSparseProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceSparseProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceSparseProperties implements Structure.ByReference { }

	public boolean residencyStandard2DBlockShape;
	public boolean residencyStandard2DMultisampleBlockShape;
	public boolean residencyStandard3DBlockShape;
	public boolean residencyAlignedMipSize;
	public boolean residencyNonResidentStrict;
}
