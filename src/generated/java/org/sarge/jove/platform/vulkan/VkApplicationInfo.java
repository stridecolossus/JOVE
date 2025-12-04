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
public class VkApplicationInfo implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public String pApplicationName;
	public int applicationVersion;
	public String pEngineName;
	public int engineVersion;
	public int apiVersion;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("pApplicationName"),
			JAVA_INT.withName("applicationVersion"),
			PADDING,
			POINTER.withName("pEngineName"),
			JAVA_INT.withName("engineVersion"),
			JAVA_INT.withName("apiVersion")
		);
	}
}
