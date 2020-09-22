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
	public static final String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";

	/**
	 * Swap-chain extension.
	 */
	public static final String EXTENSION_SWAP_CHAIN = "VK_KHR_swapchain";

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
	 * @return New integer-by-reference
	 */
	public IntByReference integer() {
		return new IntByReference();
	}

	/**
	 * @return New pointer-by-reference
	 */
	public PointerByReference pointer() {
		return new PointerByReference();
	}

	/**
	 * @return Extensions function
	 */
	public VulkanFunction<VkExtensionProperties> extensions() {
		return (count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
	}

	/**
	 * @return Validation layers function
	 */
	public VulkanFunction<VkLayerProperties> layers() {
		return (count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
	}
}
