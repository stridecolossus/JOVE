package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.texture.Image;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>Vulkan image</i> stores image data.
 * @author Sarge
 */
public class VulkanImage extends LogicalDeviceHandle {
	/**
	 * Helper - Builds image extents.
	 * @param extents Extents array
	 * @return Image extents
	 * @throws IllegalArgumentException if the number of extents is not 1..3
	 * @throws IllegalArgumentException if any extent is not one-or-more
	 */
	public static VkExtent3D extents(int... extents) {
		// Init extents
		final VkExtent3D result = new VkExtent3D();
		result.depth = 1;
		result.height = 1;

		// Populate extents from array
		switch(extents.length) {
		case 3:
			result.depth = oneOrMore(extents[2]);
			// $FALL-THROUGH$

		case 2:
			result.height = oneOrMore(extents[1]);
			// $FALL-THROUGH$

		case 1:
			result.width = oneOrMore(extents[0]);
			break;

		default:
			throw new IllegalArgumentException("Invalid extents");
		}

		return result;
	}

	/**
	 * Helper - Clones the given image extents.
	 * @param extents Extents
	 * @return Clone
	 */
	public static VkExtent3D clone(VkExtent3D extents) {
		final VkExtent3D clone = new VkExtent3D();
		clone.width = extents.width;
		clone.height = extents.height;
		clone.depth = extents.depth;
		return clone;
	}

	private final Set<VkImageAspectFlag> aspect;
	private final VkFormat format;
	private final VkExtent3D extents;

	private VkImageLayout layout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;

	/**
	 * Constructor.
	 * @param handle		Image handle
	 * @param dev			Logical device
	 * @param aspect		Image aspect(s)
	 * @param format		Image format
	 * @param extents		Image extents
	 */
	public VulkanImage(Pointer handle, LogicalDevice dev, Set<VkImageAspectFlag> aspect, VkFormat format, VkExtent3D extents) {
		super(handle, dev, lib -> lib::vkDestroyImage); // TODO - not required for swap-chain images
		this.aspect = Set.copyOf(aspect);
		this.format = notNull(format);
		this.extents = clone(extents);
	}

	/**
	 * @return Image aspect(s)
	 */
	public Set<VkImageAspectFlag> aspect() {
		return aspect;
	}

	/**
	 * @return Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Image extents
	 */
	public VkExtent3D extents() {
		return clone(extents);
	}

	/**
	 * @return Image layout
	 */
	public VkImageLayout layout() {
		return layout;
	}

	/**
	 * Creates and configures a pipeline barrier for this image.
	 * @return New pipeline barrier
	 */
	public Barrier barrier() {
		return new Barrier();
	}

	/**
	 * Creates and configures a copier for this image.
	 * @return New image copier
	 */
	public Copier copier() {
		return new Copier();
	}

	/**
	 * Builder for a Vulkan image.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final VkImageCreateInfo info = new VkImageCreateInfo();
		private final Set<VkMemoryPropertyFlag> props = new StrictSet<>();
		private final Set<VkImageAspectFlag> aspect = new StrictSet<>();

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			init();
		}

		/**
		 * Initialises the image descriptor.
		 */
		private void init() {
			type(VkImageType.VK_IMAGE_TYPE_2D);
			mipLevels(1);
			arrayLayers(1);
			tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL);
			initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
			samples(VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT);
			mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
		}

		/**
		 * Initialises the format and extents of this image.
		 * @param image Image
		 */
		public Builder image(Image image) {
			// Determine Vulkan format for this image
			final Image.Format f = image.format();
			final Vertex.Component.Type type = Vertex.Component.Type.of(f.type());
			final VkFormat format = new VulkanHelper.FormatBuilder()
				.components(f.components())
				.bytes(1)
				.signed(false)
				.type(type)
				.build();

			// Initialise relevant fields
			format(format);
			extents(image.size());

			return this;
		}

		/**
		 * Sets the aspect of this image.
		 * @param aspect Image aspect
		 */
		public Builder aspect(VkImageAspectFlag aspect) {
			this.aspect.add(aspect);
			return this;
		}

		/**
		 * Sets the type of this image.
		 * @param type Image type
		 */
		public Builder type(VkImageType type) {
			info.imageType = notNull(type);
			return this;
		}

		/**
		 * Sets the extents of this image.
		 * @param extents Image extents
		 */
		public Builder extents(VkExtent3D extents) {
			info.extent = VulkanImage.clone(extents);
			return this;
		}

		/**
		 * Convenience method to set the extents of a 2D image.
		 * @param dim Image dimensions
		 */
		public Builder extents(Dimensions dim) {
			info.extent = VulkanImage.extents(dim.width, dim.height);
			return this;
		}

		/**
		 * Sets the number of mipmap levels.
		 * @param mipLevels Mipmap levels
		 */
		public Builder mipLevels(int mipLevels) {
			info.mipLevels = oneOrMore(mipLevels);
			return this;
		}

		/**
		 * Sets the number of layers.
		 * @param layers Layers
		 */
		public Builder arrayLayers(int layers) {
			info.arrayLayers = oneOrMore(layers);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Format
		 */
		public Builder format(VkFormat format) {
			info.format = notNull(format);
			// TODO - check format supported by device
			return this;
		}

		/**
		 * Sets the image tiling.
		 * @param tiling Tiling
		 */
		public Builder tiling(VkImageTiling tiling) {
			info.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Sets the initial layout of this image.
		 * @param layout Initial layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			info.initialLayout = notNull(layout);
			return this;
		}

		/**
		 * Adds a usage flag.
		 * @param usage Usage
		 */
		public Builder usage(VkImageUsageFlag usage) {
			info.usage |= usage.value();
			return this;
		}

		/**
		 * Sets the number of samples for this image.
		 * @param samples Samples
		 */
		public Builder samples(VkSampleCountFlag samples) {
			info.samples = samples.value();
			return this;
		}

		/**
		 * Sets the sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			info.sharingMode = notNull(mode);
			return this;
		}

		/**
		 * Adds a memory property for this image.
		 * @param prop Memory property
		 */
		public Builder property(VkMemoryPropertyFlag prop) {
			props.add(prop);
			return this;
		}

		/**
		 * Constructs this image.
		 * @return New image
		 */
		public VulkanImage build() {
			// Validate
			if(info.extent == null) throw new IllegalArgumentException("Image extents not specified");
			if(info.format == null) throw new IllegalArgumentException("Image format not specified");
			// TODO - check format supported by device
			if(aspect.isEmpty()) throw new IllegalArgumentException("Image aspect(s) not specified");
			VALID_TRANSITIONS.validate(aspect, info.initialLayout);

			// Allocate image
			final Vulkan vulkan = dev.vulkan();
			final VulkanLibraryImage lib = vulkan.api();
			final PointerByReference handle = vulkan.factory().reference();
			check(lib.vkCreateImage(dev.handle(), info, null, handle));

			// Determine memory requirements
			final VkMemoryRequirements reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev.handle(), handle.getValue(), reqs);

			// Allocate image memory
			final Pointer mem = dev.allocate(reqs, props);

			// Bind image memory
			check(lib.vkBindImageMemory(dev.handle(), handle.getValue(), mem, 0L));

			// Create image
			return new VulkanImage(handle.getValue(), dev, aspect, info.format, info.extent);
		}
	}

	/**
	 * Valid transitions helper.
	 */
	private static class ValidTransitions {
		private final Map<VkImageLayout, Set<VkImageAspectFlag>> transitions = new HashMap<>();

		/**
		 * Constructor.
		 */
		private ValidTransitions() {
			// Colour images
			add(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
			add(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

			// Depth image
			add(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
		}

		/**
		 * Registers a valid transition.
		 */
		private void add(VkImageAspectFlag aspect, VkImageLayout layout) {
			transitions.computeIfAbsent(layout, ignored -> new HashSet<>()).add(aspect);
		}

		/**
		 * Validates an image layout transition.
		 * @param aspect		Image aspect(s)
		 * @param layout		Destination layout
		 * @throws IllegalArgumentException if the aspect and layout are not compatible
		 */
		private void validate(Set<VkImageAspectFlag> aspect, VkImageLayout layout) {
			// All transitions from undefined are valid
			if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) {
				return;
			}

			// Otherwise check for valid transition
			final var valid = transitions.get(layout);
			if((valid == null) || valid.stream().noneMatch(aspect::contains)) {
				throw new IllegalStateException(String.format("Invalid image transition: layout=%s aspects=%s", layout, aspect));
			}
		}
	}

	private static final ValidTransitions VALID_TRANSITIONS = new ValidTransitions();

	/**
	 * A <i>pipeline barrier</i> is used to transition the layout of this image.
	 * <p>
	 * Example:
	 * <pre>
	 * image
	 *     .barrier()
	 *     .layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
	 *     .source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
	 *     .destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
	 *     .destination(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
	 *     .transition(pool);
	 * </pre>
	 * where {@code pool} is this case would be a transfer command pool.
	 */
	public class Barrier {
		private final VkImageMemoryBarrier barrier = new VkImageMemoryBarrier();
		private VkPipelineStageFlag src = VkPipelineStageFlag.VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT;
		private VkPipelineStageFlag dest = VkPipelineStageFlag.VK_PIPELINE_STAGE_ALL_GRAPHICS_BIT;

		/**
		 * Constructor.
		 */
		private Barrier() {
			// Init descriptor
			final VulkanImage image = VulkanImage.this;
			barrier.image = image.handle();
			barrier.oldLayout = image.layout;
			barrier.srcQueueFamilyIndex = -1;
			barrier.dstQueueFamilyIndex = -1;

			// Init resource range
			// TODO - expose as builder?
			barrier.subresourceRange.aspectMask = IntegerEnumeration.mask(image.aspect);
			barrier.subresourceRange.baseMipLevel = 0;
			barrier.subresourceRange.levelCount = 1;
			barrier.subresourceRange.baseArrayLayer = 0;
			barrier.subresourceRange.layerCount = 1;
		}

		/**
		 * Sets the new layout.
		 * @param layout New layout
		 */
		public Barrier layout(VkImageLayout layout) {
			barrier.newLayout = notNull(layout);
			return this;
		}

		/**
		 * Adds a source access flag.
		 * @param access Access flag
		 */
		public Barrier source(VkAccessFlag access) {
			barrier.srcAccessMask |= access.value();
			return this;
		}

		/**
		 * Sets the source pipeline stage.
		 * @param src Pipeline stage
		 */
		public Barrier source(VkPipelineStageFlag src) {
			this.src = notNull(src);
			return this;
		}

		/**
		 * Adds a destination access flag.
		 * @param access Access flag
		 */
		public Barrier destination(VkAccessFlag access) {
			barrier.dstAccessMask |= access.value();
			return this;
		}

		/**
		 * Sets the destination pipeline stage.
		 * @param dest Pipeline stage
		 */
		public Barrier destination(VkPipelineStageFlag dest) {
			this.dest = notNull(dest);
			return this;
		}

		/**
		 * Creates a command for this barrier.
		 * @return New barrier command
		 * @throws IllegalArgumentException if the new layout is not valid for this image
		 */
		public Command command() {
			if(barrier.newLayout == null) throw new IllegalArgumentException("New layout not specified");
			if(barrier.newLayout == VulkanImage.this.layout) throw new IllegalArgumentException("Cannot transition to existing layout: " + layout);
			VALID_TRANSITIONS.validate(aspect, barrier.newLayout);
			return (lib, cmd) -> {
				lib.vkCmdPipelineBarrier(cmd, src.value(), dest.value(), 0, 0, null, 0, null, 1, new VkImageMemoryBarrier[]{barrier});
				layout = barrier.newLayout;
			};
		}

		/**
		 * Helper - Performs this pipeline barrier transition.
		 * The transition is executed using a once-only command buffer and blocks until completion.
		 * @param pool Command pool
		 * @throws IllegalArgumentException if the new layout is not valid for this image
		 * @see Command.Pool#allocate(Command)
		 * @see #command()
		 */
		public synchronized void transition(Command.Pool pool) {
			final Command.Buffer buffer = pool.allocate(command());
			buffer.submit();
			buffer.queue().waitIdle();
			buffer.free();
		}
	}

	/**
	 * An <i>image copier</i> is used to copy this image to/from a data buffer.
	 */
	public class Copier {
		private final VkBufferImageCopy copy = new VkBufferImageCopy();

		/**
		 * Constructor.
		 */
		private Copier() {
			copy.bufferOffset = 0;
			copy.bufferRowLength = 0;
			copy.bufferImageHeight = 0;
			copy.imageSubresource.aspectMask = IntegerEnumeration.mask(aspect);
			copy.imageSubresource.mipLevel = 0;
			copy.imageSubresource.baseArrayLayer = 0;
			copy.imageSubresource.layerCount = 1;
			copy.imageExtent = extents;
		}

		/**
		 * Creates a command to copy <b>from</b> the given data buffer to this image.
		 * @param buffer Source buffer
		 * @return Copy command
		 */
		public Command from(VulkanDataBuffer buffer) {
			// TODO
			// - validate
			// - layout
			// - copy descriptor
			return (lib, cmd) -> lib.vkCmdCopyBufferToImage(cmd, buffer.handle(), VulkanImage.this.handle(), layout, 1, copy);
		}

		// TODO - copy-to-buffer command
		// TODO - configure VkBufferImageCopy
	}
}
