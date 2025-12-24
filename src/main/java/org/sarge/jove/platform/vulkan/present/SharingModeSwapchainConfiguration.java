package org.sarge.jove.platform.vulkan.present;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkSharingMode;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;
import org.sarge.jove.platform.vulkan.present.VulkanSurface.Properties;

/**
 * The <i>sharing mode swapchain configuration</i> initialises the {@link VkSharingMode} of the swapchain depending on the queue families used by the application.
 * @see Swapchain.Builder#concurrent(Collection)
 * @author Sarge
 */
public class SharingModeSwapchainConfiguration implements SwapchainConfiguration {
	private final Set<Family> families;

	/**
	 * Constructor.
	 * @param families Queue families
	 */
	public SharingModeSwapchainConfiguration(Collection<Family> families) {
		this.families = Set.copyOf(families);
	}

	@Override
	public void configure(Swapchain.Builder builder, Properties properties) {
		if(families.size() != 1) {
			builder.concurrent(families);
		}
	}
}
