package org.sarge.jove.platform.vulkan.api;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure.FieldOrder;

/**
 * Mock Vulkan structure.
 * Note has to be declared in its own compilation unit and must have at least one field.
 * @author Sarge
 */
@FieldOrder("field")
public class MockStructure extends VulkanStructure {
	public int field;
}
