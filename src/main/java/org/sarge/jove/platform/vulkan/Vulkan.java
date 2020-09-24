package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>Vulkan</i> context encapsulates global access to the Vulkan API and supporting functionality.
 * @author Sarge
 */
public class Vulkan {
	/**
	 * Debug utility extension.
	 */
	public static final String DEBUG_UTILS = "VK_EXT_debug_utils";

	/**
	 * Swap-chain extension.
	 */
	public static final String SWAP_CHAIN = "VK_KHR_swapchain";

	/**
	 * API function for extensions supported by this Vulkan implementation.
	 */
	public static final VulkanFunction<VkExtensionProperties> EXTENSIONS = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);

	/**
	 * API function for validation layers supported by this Vulkan implementation.
	 */
	public static final VulkanFunction<VkLayerProperties> LAYERS = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);

	private final VulkanLibrary api;

	/**
	 * Constructor.
	 * @param api Vulkan API
	 */
	public Vulkan(VulkanLibrary api) {
		this.api = notNull(api);
	}

	/**
	 * @return Vulkan API
	 */
	public VulkanLibrary api() {
		return api;
	}

	/**
	 * Factory for integer-by-reference values.
	 * @return New integer-by-reference
	 */
	public IntByReference integer() {
		return new IntByReference();
	}

	/**
	 * Factory for pointer-by-reference values.
	 * @return New pointer-by-reference
	 */
	public PointerByReference pointer() {
		return new PointerByReference();
	}
}
