package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"pApplicationName",
	"applicationVersion",
	"pEngineName",
	"engineVersion",
	"apiVersion"
})
public class VkApplicationInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO;
	public Pointer pNext;
	public String pApplicationName;
	public int applicationVersion;
	public String pEngineName;
	public int engineVersion;
	public int apiVersion;
}
