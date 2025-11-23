package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkAccess.*;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStage.TRANSFER;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier.BarrierType.ImageBarrier;
import org.sarge.jove.platform.vulkan.render.Swapchain;

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
	private final Command.Pool pool;
	private final LogicalDevice device;

	/**
	 * Constructor.
	 * @param pool Transfer command pool
	 */
	public CaptureTask(Command.Pool pool) {
		this.pool = requireNonNull(pool);
		this.device = pool.device();
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
		final LogicalDevice device = swapchain.device();
		final DefaultImage screenshot = screenshot(device, allocator, image.descriptor());

		// Init copy command
		final Image.Library library = swapchain.device().library();
		final Command copy = ImageCopyCommand.of(image, screenshot, library);

		// Build screenshot task
		final Command.Buffer buffer = pool
				.allocate(1, true)
				.getFirst()
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

	/**
	 * Creates a screenshot image.
	 * @param device		Logical device
	 * @param allocator		Memory allocator
	 * @param target		Target image descriptor
	 */
	private static DefaultImage screenshot(LogicalDevice device, Allocator allocator, Image.Descriptor target) {
		// Create descriptor
		final var descriptor = new Image.Descriptor.Builder()
				.type(VkImageType.TWO_D)
				.aspect(VkImageAspect.COLOR)
				.extents(target.extents().size())
				.format(VkFormat.R8G8B8A8_UNORM) // TODO
				.build();

		// Init image memory properties
		final var properties = new MemoryProperties.Builder<VkImageUsageFlag>()
				.usage(VkImageUsageFlag.TRANSFER_DST)
				.required(VkMemoryProperty.HOST_VISIBLE)
				.required(VkMemoryProperty.HOST_COHERENT)
				.build();

		// Create screenshot image
		return new DefaultImage.Builder()
				.descriptor(descriptor)
				.properties(properties)
				.tiling(VkImageTiling.LINEAR)
				.build(allocator);
	}

	/**
	 * Transitions the screenshot to a copy destination.
	 */
	private Barrier destination(Image screenshot) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
 				.add(Set.of(), Set.of(TRANSFER_WRITE), new ImageBarrier(screenshot, UNDEFINED, TRANSFER_DST_OPTIMAL))
				.build(device);
	}

	/**
	 * Transitions the swapchain image to a copy source.
	 */
	private Barrier source(Image image) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
 				.add(Set.of(MEMORY_READ), Set.of(TRANSFER_READ), new ImageBarrier(image, PRESENT_SRC_KHR, TRANSFER_SRC_OPTIMAL))
				.build(device);
	}

	/**
	 * Transitions the completed screenshot.
	 */
	private Barrier prepare(Image screenshot) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
 				.add(Set.of(TRANSFER_WRITE), Set.of(MEMORY_READ), new ImageBarrier(screenshot, TRANSFER_DST_OPTIMAL, GENERAL))
				.build(device);
	}

	/**
	 * Restores the swapchain image to its initial state.
	 */
	private Barrier restore(Image image) {
		return new Barrier.Builder()
				.source(TRANSFER)
				.destination(TRANSFER)
 				.add(Set.of(TRANSFER_READ), Set.of(MEMORY_READ), new ImageBarrier(image, TRANSFER_SRC_OPTIMAL, PRESENT_SRC_KHR))
				.build(device);
	}
}
