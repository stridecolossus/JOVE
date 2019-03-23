package org.sarge.jove.platform.vulkan;

import java.nio.ByteBuffer;

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
public class VkShaderModuleCreateInfo extends Structure {
	public static class ByValue extends VkShaderModuleCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkShaderModuleCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public long codeSize;
	public ByteBuffer pCode;
}
