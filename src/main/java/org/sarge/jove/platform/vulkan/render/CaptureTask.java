package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.VkAccess.*;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStage.TRANSFER;
import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;

/**
 * The <i>capture task</i> is used to capture a screenshot from the swapchain.
 * <p>
 * Notes:
 * <ul>
 * <li>Assumes swapchain images have been created with {@link VkImageUsageFlag#TRANSFER_SRC}</li>
 * <li>The resultant image has a {@link VkFormat#R8G8B8A8_UNORM} format</li>
 * <li>Screenshot capture is a blocking operation</li>
 * </ul>
 * @author Sarge
 */
public class CaptureTask {
	private final Pool pool;

	/**
	 * Constructor.
	 * @param pool Transfer command pool
	 */
	public CaptureTask(Pool pool) {
		this.pool = requireNonNull(pool);
	}

	/**
	 * Captures a screenshot from the given swapchain.
	 * @param swapchain Swapchain to capture
	 * @return Screenshot
	 */
	public Image capture(Swapchain swapchain, Allocator allocator) {
		// Retrieve latest rendered swapchain image
		final Image image = swapchain.latest().image();

		// Create destination screenshot image
		final DeviceContext dev = swapchain.device();
		final DefaultImage screenshot = screenshot(dev, allocator, image.descriptor());

		// Init copy command
		final Command copy = ImageCopyCommand.of(image, screenshot);

		// Build screenshot task
		final Command.Buffer buffer = pool
				.primary()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
					.add(destination(screenshot))
					.add(source(image))
					.add(copy)
					.add(prepare(screenshot))
					.add(restore(image))
				.end();

		// submit and wait for screenshot
		Work.submit(buffer);

		return screenshot;
	}

	// TODO - factor out target image helper and overload? but screenshot ~ swapchain image

	/**
	 * Creates a screenshot image.
	 * @param dev			Logical device
	 * @param allocator		Memory allocator
	 * @param target		Target image descriptor
	 */
	private static DefaultImage screenshot(DeviceContext dev, Allocator allocator, Image.Descriptor target) {
		// Create descriptor
		final var descriptor = new Image.Descriptor.Builder()
				.type(VkImageType.TWO_D)
				.aspect(VkImageAspect.COLOR)
				.extents(target.extents().size())
				.format(VkFormat.R8G8B8A8_UNORM) // TODO
				.build();

		// Init image memory properties
		final var props = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.TRANSFER_DST)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.required(VkMemoryProperty.HOST_COHERENT)
				.build();

		// Create screenshot image
		return new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(props)
				.tiling(VkImageTiling.LINEAR)
				.build(dev, allocator);
	}

	/**
	 * Transitions the screenshot to a copy destination.
	 */
	private static Barrier destination(Image screenshot) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
				.image(screenshot)
					.newLayout(TRANSFER_DST_OPTIMAL)
					.destination(TRANSFER_WRITE)
					.build()
				.build();
	}

	/**
	 * Transitions the swapchain image to a copy source.
	 */
	private static Barrier source(Image image) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
				.image(image)
					.oldLayout(PRESENT_SRC_KHR)
					.newLayout(TRANSFER_SRC_OPTIMAL)
					.source(MEMORY_READ)
					.destination(TRANSFER_READ)
					.build()
				.build();
	}

	/**
	 * Transitions the completed screenshot.
	 */
	private static Barrier prepare(Image screenshot) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
				.image(screenshot)
					.oldLayout(TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.GENERAL)
					.source(TRANSFER_WRITE)
					.destination(MEMORY_READ)
					.build()
				.build();
	}

	/**
	 * Restores the swapchain image to its initial state.
	 */
	private static Barrier restore(Image image) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
				.image(image)
					.oldLayout(TRANSFER_SRC_OPTIMAL)
					.newLayout(PRESENT_SRC_KHR)
					.source(TRANSFER_READ)
					.destination(MEMORY_READ)
					.build()
				.build();
	}
}
