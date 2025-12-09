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
public class VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int advancedBlendMaxColorAttachments;
	public boolean advancedBlendIndependentBlend;
	public boolean advancedBlendNonPremultipliedSrcColor;
	public boolean advancedBlendNonPremultipliedDstColor;
	public boolean advancedBlendCorrelatedOverlap;
	public boolean advancedBlendAllOperations;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("advancedBlendMaxColorAttachments"),
			JAVA_INT.withName("advancedBlendIndependentBlend"),
			JAVA_INT.withName("advancedBlendNonPremultipliedSrcColor"),
			JAVA_INT.withName("advancedBlendNonPremultipliedDstColor"),
			JAVA_INT.withName("advancedBlendCorrelatedOverlap"),
			JAVA_INT.withName("advancedBlendAllOperations")
		);
	}
}
