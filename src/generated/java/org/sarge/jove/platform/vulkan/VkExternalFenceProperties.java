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
public class VkExternalFenceProperties implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public EnumMask<VkExternalFenceHandleTypeFlags> exportFromImportedHandleTypes;
	public EnumMask<VkExternalFenceHandleTypeFlags> compatibleHandleTypes;
	public EnumMask<VkExternalFenceFeatureFlags> externalFenceFeatures;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("exportFromImportedHandleTypes"),
			JAVA_INT.withName("compatibleHandleTypes"),
			JAVA_INT.withName("externalFenceFeatures")
		);
	}
}
