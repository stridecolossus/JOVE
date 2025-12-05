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
public class VkPhysicalDeviceFragmentDensityMapPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkExtent2D minFragmentDensityTexelSize;
	public VkExtent2D maxFragmentDensityTexelSize;
	public boolean fragmentDensityInvocations;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("minFragmentDensityTexelSize"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("width"),
				JAVA_INT.withName("height")
			).withName("maxFragmentDensityTexelSize"),
			JAVA_INT.withName("fragmentDensityInvocations"),
			PADDING
		);
	}
}
