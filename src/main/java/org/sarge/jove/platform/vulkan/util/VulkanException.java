package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.util.IntegerEnumeration;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * <p>
 * Note that Vulkan API methods return {@code int} rather than {@link VkResult} explicitly for forward-compatibility.
 * <p>
 * @author Sarge
 */
public class VulkanException extends RuntimeException {
	private final int result;

	/**
	 * Constructor.
	 * @param result Vulkan result code
	 */
	public VulkanException(int result) {
		super(String.format("%s[%d]", reason(result), result));
		this.result = result;
	}

	/**
	 * @return Vulkan error code
	 */
	public int result() {
		return result;
	}

	/**
	 * Helper - Maps the given Vulkan result code to the corresponding reason token.
	 * @param result Vulkan result code
	 * @return Reason code
	 */
	private static String reason(int result) {
		try {
			return IntegerEnumeration.mapping(VkResult.class).map(result).name();
		}
		catch(IllegalArgumentException e) {
			return "Unknown error code";
		}
	}
}
