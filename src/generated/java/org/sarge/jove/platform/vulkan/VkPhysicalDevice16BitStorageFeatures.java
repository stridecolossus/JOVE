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
public class VkPhysicalDevice16BitStorageFeatures implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public boolean storageBuffer16BitAccess;
	public boolean uniformAndStorageBuffer16BitAccess;
	public boolean storagePushConstant16;
	public boolean storageInputOutput16;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("storageBuffer16BitAccess"),
			JAVA_INT.withName("uniformAndStorageBuffer16BitAccess"),
			JAVA_INT.withName("storagePushConstant16"),
			JAVA_INT.withName("storageInputOutput16")
		);
	}
}
