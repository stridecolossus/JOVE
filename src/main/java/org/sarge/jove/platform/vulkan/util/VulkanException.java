package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkResult;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * <p>
 * Note that Vulkan API methods return {@code int} rather than {@link VkResult} explicitly for forward-compatibility.
 * <p>
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
		super(String.format("[%d]%s: %s", result, reason(result), message));
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
