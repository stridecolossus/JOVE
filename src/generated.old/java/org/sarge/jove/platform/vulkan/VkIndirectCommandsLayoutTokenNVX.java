package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"tokenType",
	"bindingUnit",
	"dynamicCount",
	"divisor"
})
public class VkIndirectCommandsLayoutTokenNVX extends Structure {
	public static class ByValue extends VkIndirectCommandsLayoutTokenNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsLayoutTokenNVX implements Structure.ByReference { }
	
	public int tokenType;
	public int bindingUnit;
	public int dynamicCount;
	public int divisor;
}
