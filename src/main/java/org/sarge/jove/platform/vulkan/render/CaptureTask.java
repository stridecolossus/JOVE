package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;

/**
 * The <i>capture task</i> is used to capture a screenshot from the swapchain.
 * @author Sarge
 */
public class CaptureTask {
	private final AllocationService allocator;
	private final Pool pool;

	/**
	 * Constructor.
	 * @param allocator 	Memory allocator
	 * @param pool			Transfer command pool
	 */
	public CaptureTask(AllocationService allocator, Pool pool) {
		this.allocator = notNull(allocator);
		this.pool = notNull(pool);
	}

	/**
	 * Captures a screenshot from the given swapchain.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The resultant image has a {@link VkFormat#R8G8B8A8_UNORM} format</li>
	 * <li>This is a blocking operation</li>
	 * </ul>
	 * @param swapchain Swapchain to capture
	 * @return Screenshot
	 */
	public Image capture(Swapchain swapchain) {
		// Retrieve latest rendered swapchain image
		final Image image = swapchain.latest().image();

		// Create destination screenshot image
		final DeviceContext dev = swapchain.device();
		final DefaultImage screenshot = screenshot(dev, image.descriptor());

		// Init copy command
		final Command copy = ImageCopyCommand.of(image, screenshot);

		// Submit screenshot task
		pool
				.allocate()
				.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
					.add(destination(screenshot))
					.add(source(image))
					.add(copy)
					.add(prepare(screenshot))
					.add(restore(image))
				.end()
				.submit();

		return screenshot;
	}

/*
		// TODO
		try {
			final ByteBuffer bb = screenshot.memory().map().buffer();
			final byte[] bytes = BufferHelper.array(bb);

			final Dimensions size = screenshot.descriptor().extents().size();
		    final DataBufferByte data = new DataBufferByte(bytes, bytes.length);
		    final WritableRaster raster = Raster.createInterleavedRaster(data, size.width(), size.height(), size.width() * 4, 4, new int[]{2, 1, 0}, null);

		    final ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		    final var img = new BufferedImage(cm, raster, false, null);

		    System.out.println(img);

		    final boolean done = ImageIO.write(img, "jpg", new File("output.jpg"));
		    System.out.println("done="+done);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	*/

	/**
	 * Creates a screenshot image.
	 * @param dev			Logical device
	 * @param target		Target image descriptor
	 */
	private DefaultImage screenshot(DeviceContext dev, Descriptor target) {
		// Create descriptor
		final Descriptor descriptor = new Descriptor.Builder()
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
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.TRANSFER)
				.image(screenshot)
					.newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.destination(VkAccess.TRANSFER_WRITE)
					.build()
				.build();
	}

	/**
	 * Transitions the swapchain image to a copy source.
	 */
	private static Barrier source(Image image) {
		return new Barrier.Builder()
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.TRANSFER)
				.image(image)
					.oldLayout(VkImageLayout.PRESENT_SRC_KHR)
					.newLayout(VkImageLayout.TRANSFER_SRC_OPTIMAL)
					.source(VkAccess.MEMORY_READ)
					.destination(VkAccess.TRANSFER_READ)
					.build()
				.build();
	}

	/**
	 * Transitions the completed screenshot.
	 */
	private static Barrier prepare(Image screenshot) {
		return new Barrier.Builder()
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.TRANSFER)
				.image(screenshot)
					.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.GENERAL)
					.source(VkAccess.TRANSFER_WRITE)
					.destination(VkAccess.MEMORY_READ)
					.build()
				.build();
	}

	/**
	 * Restores the swapchain image to its initial state.
	 */
	private static Barrier restore(Image image) {
		return new Barrier.Builder()
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.TRANSFER)
				.image(image)
					.oldLayout(VkImageLayout.TRANSFER_SRC_OPTIMAL)
					.newLayout(VkImageLayout.PRESENT_SRC_KHR)
					.source(VkAccess.TRANSFER_READ)
					.destination(VkAccess.MEMORY_READ)
					.build()
				.build();
	}
}
