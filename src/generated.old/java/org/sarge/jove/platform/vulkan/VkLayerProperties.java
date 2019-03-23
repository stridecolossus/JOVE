package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"layerName",
	"specVersion",
	"implementationVersion",
	"description"
})
public class VkLayerProperties extends Structure {
	public static class ByValue extends VkLayerProperties implements Structure.ByValue { }
	public static class ByReference extends VkLayerProperties implements Structure.ByReference { }
	
	public final byte[] layerName = new byte[256];
	public int specVersion;
	public int implementationVersion;
	public final byte[] description = new byte[256];
}
