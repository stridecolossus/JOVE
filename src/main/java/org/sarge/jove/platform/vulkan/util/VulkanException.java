package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.platform.vulkan.VkResult;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * <p>
 * Note that Vulkan API methods generally return {@code int} rather than {@link VkResult} explicitly for forward-compatibility.
 * <p>
 * @author Sarge
 */
public class VulkanException extends RuntimeException {
	/**
	 * Constructor.
	 * @param result Vulkan result
	 */
	public VulkanException(VkResult result) {
		super(String.format("%s[%d]", result.name(), result.value()));
	}
}
