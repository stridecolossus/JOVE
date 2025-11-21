package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>default image</i> is a Vulkan image or texture managed by the application.
 * @author Sarge
 */
public class DefaultImage extends VulkanObject implements Image {
	private final Descriptor descriptor;
	private final DeviceMemory memory;

	/**
	 * Constructor.
	 * @param handle			Handle
	 * @param device			Logical device
	 * @param descriptor		Descriptor for this image
	 * @param memory			Device memory
	 */
	DefaultImage(Handle handle, LogicalDevice device, Descriptor descriptor, DeviceMemory memory) {
		super(handle, device);
		this.descriptor = requireNonNull(descriptor);
		this.memory = requireNonNull(memory);
	}

	@Override
	public Descriptor descriptor() {
		return descriptor;
	}

	/**
	 * @return Device memory for this image
	 */
	public DeviceMemory memory() {
		return memory;
	}

	@Override
	protected Destructor<DefaultImage> destructor() {
		final Library library = this.device().library();
		return library::vkDestroyImage;
	}

	@Override
	protected void release() {
		if(!memory.isDestroyed()) {
			memory.destroy();
		}
	}

	/**
	 * Builder for a default image.
	 */
	public static class Builder {
		private static final ReverseMapping<VkSampleCount> SAMPLES = ReverseMapping.mapping(VkSampleCount.class);

		private Descriptor descriptor;
		private MemoryProperties<VkImageUsageFlag> properties;
		private final Set<VkImageCreateFlag> flags = new HashSet<>();
		private VkSampleCount samples = VkSampleCount.COUNT_1;
		private VkImageTiling tiling = VkImageTiling.OPTIMAL;
		private VkImageLayout layout = VkImageLayout.UNDEFINED;

		/**
		 * Sets the descriptor for this image.
		 * @param descriptor Image descriptor
		 */
		public Builder descriptor(Descriptor descriptor) {
			this.descriptor = requireNonNull(descriptor);
			return this;
		}
		// TODO - ctor

		/**
		 * Sets the memory properties of this image.
		 * @param props Memory properties
		 */
		public Builder properties(MemoryProperties<VkImageUsageFlag> props) {
			this.properties = requireNonNull(props);
			return this;
		}
		// TODO - ctor

		/**
		 * Adds an image creation flag.
		 * @param flag Image creation flag
		 */
		public Builder flag(VkImageCreateFlag flag) {
			requireNonNull(flag);
			flags.add(flag);
			return this;
		}

		/**
		 * Helper - Sets this image as a cube-map.
		 * @see VkImageCreateFlag#CUBE_COMPATIBLE
		 */
		public Builder cubemap() {
			return flag(VkImageCreateFlag.CUBE_COMPATIBLE);
		}

		/**
		 * Sets the number of samples (default is one).
		 * @param samples Samples-per-texel
		 * @throws IllegalArgumentException if {@link #samples} is not a valid {@link VkSampleCount}
		 */
		public Builder samples(int samples) {
			this.samples = SAMPLES.map(samples);
			return this;
		}

		/**
		 * Sets the image tiling arrangement (default is {@link VkImageTiling#TILING_OPTIMAL}).
		 * @param tiling Tiling arrangement
		 */
		public Builder tiling(VkImageTiling tiling) {
			this.tiling = requireNonNull(tiling);
			return this;
		}

		/**
		 * Sets the initial layout of this image (default is {@link VkImageLayout#UNDEFINED}).
		 * @param layout Initial layout
		 * @throws IllegalArgumentException if {@link #layout} is not {@link VkImageLayout#UNDEFINED} or {@link VkImageLayout#PREINITIALIZED}
		 */
		public Builder initialLayout(VkImageLayout layout) {
			final boolean valid = switch(layout) {
				case UNDEFINED, PREINITIALIZED -> true;
				default -> false;
			};
			if(!valid) {
				throw new IllegalArgumentException("Invalid initial layout: " + layout);
			}
			this.layout = requireNonNull(layout);
			return this;
		}

		/**
		 * Constructs this image.
		 * @param device Logical device
		 * @return New image
		 * @throws IllegalArgumentException if the image descriptor or memory properties have not been configured
		 */
		public DefaultImage build(LogicalDevice device, Allocator allocator) {
			// Validate
			if(descriptor == null) {
				throw new IllegalArgumentException("No image descriptor specified");
			}
			if(properties == null) {
				throw new IllegalArgumentException("No memory properties specified");
			}

			// Populate image structure
			final var info = new VkImageCreateInfo();
			info.flags = new EnumMask<>(flags);
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = new EnumMask<>(properties.usage());
			info.sharingMode = properties.mode();
			// TODO - queueFamilyIndexCount, pQueueFamilyIndices

			// Allocate image
			final Library library = device.library();
			final Pointer pointer = new Pointer();
			library.vkCreateImage(device, info, null, pointer);

			// Retrieve image memory requirements
			final Handle handle = pointer.handle();
			final var requirements = new VkMemoryRequirements();
			library.vkGetImageMemoryRequirements(device, handle, requirements);

			// Allocate image memory
			final DeviceMemory memory = allocator.allocate(requirements, properties);

			// Bind memory to image
			library.vkBindImageMemory(device, handle, memory, 0L);

			// Create image
			return new DefaultImage(handle, device, descriptor, memory);
		}
	}
}
