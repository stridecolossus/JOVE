package org.sarge.jove.platform.vulkan;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"codeSize",
	"pCode"
})
public class VkShaderModuleCreateInfo extends VulkanStructure {
	public static class ByValue extends VkShaderModuleCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkShaderModuleCreateInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long codeSize;
	public ByteBuffer pCode;
}