package org.sarge.jove.platform.vulkan.image;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.*;

import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>default image</i> is a Vulkan image or texture managed by the application.
 * @author Sarge
 */
public final class DefaultImage extends VulkanObject implements Image {
	private final Descriptor descriptor;
	private final DeviceMemory mem;

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Logical device
	 * @param descriptor	Descriptor for this image
	 * @param mem			Device memory
	 */
	DefaultImage(Handle handle, DeviceContext dev, Descriptor descriptor, DeviceMemory mem) {
		super(handle, dev);
		this.descriptor = requireNonNull(descriptor);
		this.mem = requireNonNull(mem);
	}

	@Override
	public Descriptor descriptor() {
		return descriptor;
	}

	/**
	 * @return Device memory for this image
	 */
	public DeviceMemory memory() {
		return mem;
	}

	@Override
	protected Destructor<DefaultImage> destructor(VulkanLibrary lib) {
		return lib::vkDestroyImage;
	}

	@Override
	protected void release() {
		if(!mem.isDestroyed()) {
			mem.destroy();
		}
	}

	/**
	 * Builder for a default image.
	 */
	public static class Builder {
		private Descriptor descriptor;
		private MemoryProperties<VkImageUsageFlag> props;
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
			this.props = requireNonNull(props);
			return this;
		}
		// TODO - ctor

		/**
		 * Adds an image creation flag.
		 * @param flag Image creation flag
		 */
		public Builder flag(VkImageCreateFlag flag) {
			flags.add(requireNonNull(flag));
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
			this.samples = IntEnum.reverse(VkSampleCount.class).map(samples);
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
			if(!valid) throw new IllegalArgumentException("Invalid initial layout: " + layout);
			this.layout = requireNonNull(layout);
			return this;
		}

		/**
		 * Constructs this image.
		 * @param dev Logical device
		 * @return New image
		 * @see DefaultImage#DefaultImage(Pointer, DeviceContext, Descriptor, DeviceMemory)
		 * @throws IllegalArgumentException if the image descriptor or memory properties have not been configured
		 */
		public DefaultImage build(DeviceContext dev, Allocator allocator) {
			// Validate
			if(descriptor == null) throw new IllegalArgumentException("No image descriptor specified");
			if(props == null) throw new IllegalArgumentException("No memory properties specified");

			// Populate image structure
			final var info = new VkImageCreateInfo();
			info.flags = new BitMask<>(flags);
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = new BitMask<>(props.usage());
			info.sharingMode = props.mode();
			// TODO - queueFamilyIndexCount, pQueueFamilyIndices

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateImage(dev, info, null, ref));

			// Retrieve image memory requirements
			final Handle handle = new Handle(ref);
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev, handle, reqs);

			// Allocate image memory
			final DeviceMemory mem = allocator.allocate(reqs, props);

			// Bind memory to image
			check(lib.vkBindImageMemory(dev, handle, mem, 0));

			// Create image
			return new DefaultImage(handle, dev, descriptor, mem);
		}
	}
}
