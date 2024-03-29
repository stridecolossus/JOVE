package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"MSize",
	"NSize",
	"KSize",
	"AType",
	"BType",
	"CType",
	"DType",
	"scope"
})
public class VkCooperativeMatrixPropertiesNV extends VulkanStructure {
	public VkStructureType sType = VkStructureType.COOPERATIVE_MATRIX_PROPERTIES_NV;
	public Pointer pNext;
	public int MSize;
	public int NSize;
	public int KSize;
	public VkComponentTypeNV AType;
	public VkComponentTypeNV BType;
	public VkComponentTypeNV CType;
	public VkComponentTypeNV DType;
	public VkScopeNV scope;
}
