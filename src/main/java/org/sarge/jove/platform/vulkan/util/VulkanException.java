package org.sarge.jove.platform.vulkan.util;

import org.sarge.jove.platform.vulkan.VkResult;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>Vulkan exception</i> wraps an error code returned by the Vulkan platform.
 * <p>
 * Note that Vulkan API methods generally return {@code int} rather than {@link VkResult} explicitly for forward-compatibility.
 * <p>
 * @author Sarge
 */
public class VulkanException extends RuntimeException {
	private static final ReverseMapping<VkResult> MAPPING = new ReverseMapping<>(VkResult.class);

	private final int result;

	/**
	 * Constructor.
	 * @param result Vulkan result code
	 */
	public VulkanException(int result) {
		this(reason(result), result);
	}

	/**
	 * Constructor.
	 * @param result Vulkan result
	 */
	public VulkanException(VkResult result) {
		this(result.name(), result.value());
	}

	/**
	 * Constructor.
	 * @param reason Reason
	 * @param result Result code
	 */
	private VulkanException(String reason, int result) {
		super(String.format("%s[%d]", reason, result));
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
			return MAPPING.map(result).name();
		}
		catch(IllegalArgumentException e) {
			return "Unknown error code";
		}
	}
}
