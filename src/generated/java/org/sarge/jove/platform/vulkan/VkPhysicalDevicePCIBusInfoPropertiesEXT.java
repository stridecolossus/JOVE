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
public class VkPhysicalDevicePCIBusInfoPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public int pciDomain;
	public int pciBus;
	public int pciDevice;
	public int pciFunction;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("pciDomain"),
			JAVA_INT.withName("pciBus"),
			JAVA_INT.withName("pciDevice"),
			JAVA_INT.withName("pciFunction")
		);
	}
}
