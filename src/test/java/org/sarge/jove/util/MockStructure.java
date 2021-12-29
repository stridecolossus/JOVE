package org.sarge.jove.util;

import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Mock JNA structure.
 * Note the structure has to be declared as public.
 * @author Sarge
 */
@FieldOrder("sType")
public class MockStructure extends VulkanStructure {
	public static class ByReference extends MockStructure implements Structure.ByReference {
	}

	public VkStructureType sType;
}
