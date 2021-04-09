package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkResult;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * @author Sarge
 */
public class VulkanException extends RuntimeException {
	public final int result;

	/**
	 * Constructor.
	 * @param result		Vulkan result code
	 * @param message		Additional message
	 */
	public VulkanException(int result, String message) {
		super(String.format("%s: [%d] %s", message, result, reason(result)));
		this.result = result;
	}

	/**
	 * Constructor.
	 * @param result Vulkan result code
	 */
	public VulkanException(int result) {
		this(result, "Vulkan error");
	}

	/**
	 * Helper - Maps the given Vulkan result code to the corresponding reason token.
	 * @param result Vulkan result code
	 * @return Reason code
	 */
	private static String reason(int result) {
		try {
			final VkResult value = IntegerEnumeration.map(VkResult.class, result);
			return value.name();
		}
		catch(IllegalArgumentException e) {
			return "Unknown error code";
		}
	}
}
