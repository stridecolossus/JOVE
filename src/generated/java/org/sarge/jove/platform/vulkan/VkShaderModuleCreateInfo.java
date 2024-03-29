package org.sarge.jove.platform.vulkan;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
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
	public VkStructureType sType = VkStructureType.SHADER_MODULE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long codeSize;
	public ByteBuffer pCode;
}
