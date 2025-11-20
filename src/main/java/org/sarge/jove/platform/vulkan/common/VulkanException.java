package org.sarge.jove.platform.vulkan.common;

import org.sarge.jove.platform.vulkan.VkResult;

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
	 * @return Error code
	 */
	public VkResult result() {
		return result;
	}
}
