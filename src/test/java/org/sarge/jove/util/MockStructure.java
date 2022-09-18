package org.sarge.jove.util;

import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.*;

/**
 * Mock JNA structure.
 * Note the structure has to be declared as public.
 * @author Sarge
 */
@FieldOrder("sType")
public class MockStructure extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.APPLICATION_INFO;
}
