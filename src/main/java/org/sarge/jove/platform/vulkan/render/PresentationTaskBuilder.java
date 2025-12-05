package org.sarge.jove.platform.vulkan.render;

import java.util.*;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.VulkanSemaphore;

/**
 * The <i>presentation task builder</i> constructs the Vulkan descriptor for swapchain presentation.
 * @author Sarge
 */
public class PresentationTaskBuilder {
	private final Map<Swapchain, Integer> images = new LinkedHashMap<>();
	private final Set<VulkanSemaphore> semaphores = new HashSet<>();

	/**
	 * Adds a swapchain image to be presented.
	 * @param swapchain		Swapchain
	 * @param index			Image index
	 * @throws IllegalArgumentException for a duplicate swapchain
	 */
	public PresentationTaskBuilder image(Swapchain swapchain, int index) {
		if(images.containsKey(swapchain)) {
			throw new IllegalArgumentException("Duplicate swapchain: " + swapchain);
		}
		images.put(swapchain, index);
		return this;
	}

	/**
	 * Adds a wait semaphore.
	 * @param semaphore Wait semaphore
	 */
	public PresentationTaskBuilder wait(VulkanSemaphore semaphore) {
		semaphores.add(semaphore);
		return this;
	}

	/**
	 * Constructs this presentation task.
	 * @return Presentation task
	 */
	public VkPresentInfoKHR build() {
		// Create presentation descriptor
		final var info = new VkPresentInfoKHR();
		info.sType = VkStructureType.PRESENT_INFO_KHR;

		// Populate wait semaphores
		info.waitSemaphoreCount = semaphores.size();
		info.pWaitSemaphores = NativeObject.handles(semaphores);

		// Populate swapchain
		info.swapchainCount = images.size();
		info.pSwapchains = NativeObject.handles(images.keySet());

		// Set image indices
		info.pImageIndices = images
				.values()
				.stream()
				.mapToInt(Integer::intValue)
				.toArray();

		return info;
	}
}
