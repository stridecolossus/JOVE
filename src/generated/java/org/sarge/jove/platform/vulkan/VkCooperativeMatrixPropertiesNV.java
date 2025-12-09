package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkCooperativeMatrixPropertiesNV implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int MSize;
	public int NSize;
	public int KSize;
	public VkComponentTypeNV AType;
	public VkComponentTypeNV BType;
	public VkComponentTypeNV CType;
	public VkComponentTypeNV DType;
	public VkScopeNV scope;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("MSize"),
			JAVA_INT.withName("NSize"),
			JAVA_INT.withName("KSize"),
			JAVA_INT.withName("AType"),
			JAVA_INT.withName("BType"),
			JAVA_INT.withName("CType"),
			JAVA_INT.withName("DType"),
			JAVA_INT.withName("scope")
		);
	}
}
