package org.sarge.jove.common;

import org.sarge.jove.platform.vulkan.VkStructureType;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Mock JNA structure.
 * Note the structure has to be declared as public.
 * @author Sarge
 */
@FieldOrder("sType")
public class MockStructure extends VulkanStructure {
	public VkStructureType sType;
}