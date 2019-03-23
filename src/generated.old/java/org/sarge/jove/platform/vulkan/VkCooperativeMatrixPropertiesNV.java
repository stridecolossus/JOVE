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
	"MSize",
	"NSize",
	"KSize",
	"AType",
	"BType",
	"CType",
	"DType",
	"scope"
})
public class VkCooperativeMatrixPropertiesNV extends Structure {
	public static class ByValue extends VkCooperativeMatrixPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkCooperativeMatrixPropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_COOPERATIVE_MATRIX_PROPERTIES_NV.value();
	public Pointer pNext;
	public int MSize;
	public int NSize;
	public int KSize;
	public int AType;
	public int BType;
	public int CType;
	public int DType;
	public int scope;
}
