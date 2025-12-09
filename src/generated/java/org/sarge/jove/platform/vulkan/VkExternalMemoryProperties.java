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
public class VkExternalMemoryProperties implements NativeStructure {
	public EnumMask<VkExternalMemoryFeatureFlags> externalMemoryFeatures;
	public EnumMask<VkExternalMemoryHandleTypeFlags> exportFromImportedHandleTypes;
	public EnumMask<VkExternalMemoryHandleTypeFlags> compatibleHandleTypes;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("externalMemoryFeatures"),
			JAVA_INT.withName("exportFromImportedHandleTypes"),
			JAVA_INT.withName("compatibleHandleTypes")
		);
	}
}
