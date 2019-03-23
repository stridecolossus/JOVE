package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"linearTilingFeatures",
	"optimalTilingFeatures",
	"bufferFeatures"
})
public class VkFormatProperties extends Structure {
	public static class ByValue extends VkFormatProperties implements Structure.ByValue { }
	public static class ByReference extends VkFormatProperties implements Structure.ByReference { }
	
	public int linearTilingFeatures;
	public int optimalTilingFeatures;
	public int bufferFeatures;
}
