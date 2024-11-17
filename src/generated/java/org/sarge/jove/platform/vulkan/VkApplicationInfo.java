package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.lib.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkApplicationInfo extends NativeStructure {
	public final VkStructureType sType = VkStructureType.APPLICATION_INFO;
	public Handle pNext;
	public String pApplicationName;
	public int applicationVersion;
	public String pEngineName;
	public int engineVersion;
	public int apiVersion;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("sType"),
				PADDING,
				POINTER.withName("pNext"),
				POINTER.withName("pApplicationName"),
				JAVA_INT.withName("applicationVersion"),
				PADDING,
				POINTER.withName("pEngineName"),
				JAVA_INT.withName("engineVersion"),
				PADDING,
				JAVA_INT.withName("apiVersion"),
				PADDING
		);
	}
}
