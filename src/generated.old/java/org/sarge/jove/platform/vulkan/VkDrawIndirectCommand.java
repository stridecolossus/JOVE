package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"vertexCount",
	"instanceCount",
	"firstVertex",
	"firstInstance"
})
public class VkDrawIndirectCommand extends Structure {
	public static class ByValue extends VkDrawIndirectCommand implements Structure.ByValue { }
	public static class ByReference extends VkDrawIndirectCommand implements Structure.ByReference { }
	
	public int vertexCount;
	public int instanceCount;
	public int firstVertex;
	public int firstInstance;
}
