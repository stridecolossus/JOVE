package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * @see VkResult
 * @author Sarge
 */
public class VulkanException extends RuntimeException {
	private final VkResult result;

	/**
	 * Constructor.
	 * @param result Vulkan result
	 */
	public VulkanException(VkResult result) {
		super(String.format("%s[%d]", result.name(), result.value()));
		this.result = result;
	}

	/**
	 * Constructor.
	 * @param code Returned success code
	 */
	public VulkanException(int code) {
		this(map(code));
	}

	private static VkResult map(int code) {
		try {
			return ReverseMapping.mapping(VkResult.class).map(code);
		}
		catch(IllegalArgumentException e) {
			return VkResult.VK_ERROR_UNKNOWN;
		}
	}

	/**
	 * @return Error code
	 */
	public VkResult result() {
		return result;
	}
}
