package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"shaderStageMask",
	"resourceUsage",
	"numPhysicalVgprs",
	"numPhysicalSgprs",
	"numAvailableVgprs",
	"numAvailableSgprs",
	"computeWorkGroupSize"
})
public class VkShaderStatisticsInfoAMD extends Structure {
	public static class ByValue extends VkShaderStatisticsInfoAMD implements Structure.ByValue { }
	public static class ByReference extends VkShaderStatisticsInfoAMD implements Structure.ByReference { }
	
	public int shaderStageMask;
	public VkShaderResourceUsageAMD resourceUsage;
	public int numPhysicalVgprs;
	public int numPhysicalSgprs;
	public int numAvailableVgprs;
	public int numAvailableSgprs;
	public final int[] computeWorkGroupSize = new int[3];
}
