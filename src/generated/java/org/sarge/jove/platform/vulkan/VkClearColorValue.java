package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"float32",
	"int32",
	"uint32"
})
public class VkClearColorValue extends VulkanStructure { //implements ByReference {
	public static class ByValue extends VkClearColorValue implements Structure.ByValue { }
	public static class ByReference extends VkClearColorValue implements Structure.ByReference { }

	public float[] float32 = new float[4];
	public int[] int32 = new int[4];
	public int[] uint32 = new int[4];
}
