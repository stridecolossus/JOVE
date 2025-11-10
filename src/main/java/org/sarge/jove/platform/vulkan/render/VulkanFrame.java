package org.sarge.jove.platform.vulkan.render;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command;

/**
 * A <i>Vulkan frame</i> tracks the state of an in-flight frame during the acquire-render-present process.
 * @author Sarge
 */
public interface VulkanFrame extends TransientObject {
	/**
	 * Acquires the next frame buffer.
	 * @param swapchain Swapchain
	 * @return Frame buffer index
	 */
	int acquire(Swapchain swapchain);

	/**
	 * Presents the next frame.
	 * @param render		Render task
	 * @param index			Frame buffer index
	 * @param swapchain		Swapchain
	 */
	void present(Command.Buffer render, int index, Swapchain swapchain);

	/**
	 * Helper - Creates an array of frame trackers.
	 * @param frames		Number of frames
	 * @param factory		Frame factory
	 * @return Array of frames
	 */
	static VulkanFrame[] array(int frames, Supplier<VulkanFrame> factory) {
		return Stream
				.generate(factory)
				.limit(frames)
				.toArray(VulkanFrame[]::new);
	}
}
