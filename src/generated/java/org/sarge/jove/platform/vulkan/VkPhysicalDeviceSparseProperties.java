package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceSparseProperties implements NativeStructure {
	public boolean residencyStandard2DBlockShape;
	public boolean residencyStandard2DMultisampleBlockShape;
	public boolean residencyStandard3DBlockShape;
	public boolean residencyAlignedMipSize;
	public boolean residencyNonResidentStrict;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
		        JAVA_INT.withName("residencyStandard2DBlockShape"),
		        JAVA_INT.withName("residencyStandard2DMultisampleBlockShape"),
		        JAVA_INT.withName("residencyStandard3DBlockShape"),
		        JAVA_INT.withName("residencyAlignedMipSize"),
		        JAVA_INT.withName("residencyNonResidentStrict")
		);
	}
}
