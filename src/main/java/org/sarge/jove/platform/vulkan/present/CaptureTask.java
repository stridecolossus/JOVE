package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkAccessFlags.*;
import static org.sarge.jove.platform.vulkan.VkImageLayout.*;
import static org.sarge.jove.platform.vulkan.VkImageUsageFlags.TRANSFER_DST;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags.*;
import static org.sarge.jove.platform.vulkan.VkPipelineStageFlags.TRANSFER;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.Barrier.BarrierType.ImageBarrier;

/**
 * The <i>capture task</i> is used to capture a screenshot from the swapchain.
 * <p>
 * Notes:
 * <ul>
 * <li>Assumes swapchain images have been created with {@link VkImageUsageFlag#TRANSFER_SRC}</li>
 * <li>Screenshot {@link #capture(Swapchain)} is a blocking operation</li>
 * </ul>
 * @author Sarge
 */
public class CaptureTask {
	private final Allocator allocator;
	private final Command.Pool pool;
	private final LogicalDevice device;

	/**
	 * Constructor.
	 * @param allocator		Memory allocator
	 * @param pool			Transfer command pool
	 * @param device		Logical device
	 */
	public CaptureTask(Allocator allocator, Command.Pool pool, LogicalDevice device) {
		this.allocator = requireNonNull(allocator);
		this.pool = requireNonNull(pool);
		this.device = requireNonNull(device);
	}

	/**
	 * Captures a screenshot from the given swapchain.
	 * @param swapchain Swapchain
	 * @return Screenshot
	 */
	public DefaultImage capture(Swapchain swapchain) {
		// Retrieve latest rendered swapchain image
		final int index = swapchain.index();
		final Image image = swapchain.attachments().get(index);

		// Init capture instance
		final Instance instance = new Instance(image);

		// Build screenshot task
		final var buffer = pool
				.allocate()
				.begin(VkCommandBufferUsageFlags.ONE_TIME_SUBMIT)
					.add(instance.destination())
					.add(instance.source())
					.add(instance.copy())
					.add(instance.prepare())
					.add(instance.restore())
				.end();

		// Submit and wait for screenshot
		Work.submit(buffer);

		return instance.screenshot;
	}

	/**
	 * Capture instance.
	 */
	private class Instance {
		private final Image attachment;
		private final DefaultImage screenshot;

		/**
		 * Constructor.
		 * @param attachment Swapchain attachment to capture
		 */
		private Instance(Image attachment) {
			this.attachment = requireNonNull(attachment);
			this.screenshot = create(attachment.descriptor());
		}

		/**
		 * Creates a screenshot image.
		 * @param target Target image descriptor
		 */
		private DefaultImage create(Image.Descriptor target) {
			// Create descriptor
			final var descriptor = new Image.Descriptor.Builder()
					.type(VkImageType.TYPE_2D)
					.aspect(VkImageAspectFlags.COLOR)
					.extents(target.extents().size())
					.format(target.format())
					.build();

			// Init image memory properties
			final var properties = new MemoryProperties.Builder<VkImageUsageFlags>()
					.usage(TRANSFER_DST)
					.required(HOST_VISIBLE)
					.required(HOST_COHERENT)
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
		public Barrier destination() {
			return new Barrier.Builder()
					.source(TRANSFER)
					.destination(TRANSFER)
	 				.add(Set.of(), Set.of(TRANSFER_WRITE), new ImageBarrier(screenshot, UNDEFINED, TRANSFER_DST_OPTIMAL))
					.build(device);
		}

		/**
		 * Transitions the swapchain image to a copy source.
		 */
		public Barrier source() {
			return new Barrier.Builder()
					.source(TRANSFER)
					.destination(TRANSFER)
	 				.add(Set.of(MEMORY_READ), Set.of(TRANSFER_READ), new ImageBarrier(attachment, PRESENT_SRC_KHR, TRANSFER_SRC_OPTIMAL))
					.build(device);
		}

		/**
		 * Copies the colour attachment to the screenshot image.
		 */
		public Command copy() {
			return ImageCopyCommand.of(attachment, screenshot, device.library());
		}

		/**
		 * Transitions the completed screenshot.
		 */
		public Barrier prepare() {
			return new Barrier.Builder()
					.source(TRANSFER)
					.destination(TRANSFER)
	 				.add(Set.of(TRANSFER_WRITE), Set.of(MEMORY_READ), new ImageBarrier(screenshot, TRANSFER_DST_OPTIMAL, GENERAL))
					.build(device);
		}

		/**
		 * Restores the swapchain image to its initial state.
		 */
		public Barrier restore() {
			return new Barrier.Builder()
					.source(TRANSFER)
					.destination(TRANSFER)
	 				.add(Set.of(TRANSFER_READ), Set.of(MEMORY_READ), new ImageBarrier(attachment, TRANSFER_SRC_OPTIMAL, PRESENT_SRC_KHR))
					.build(device);
		}
	}
}
