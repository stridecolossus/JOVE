package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.util.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>default image</i> is a Vulkan image managed by the application.
 * @author Sarge
 */
public class DefaultImage extends AbstractVulkanObject implements Image {
	private final Descriptor descriptor;
	private final DeviceMemory mem;

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Logical device
	 * @param descriptor	Descriptor for this image
	 * @param mem			Device memory
	 */
	protected DefaultImage(Pointer handle, DeviceContext dev, Descriptor descriptor, DeviceMemory mem) {
		super(handle, dev);
		this.descriptor = notNull(descriptor);
		this.mem = notNull(mem);
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(descriptor)
				.append(mem)
				.build();
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
			this.descriptor = notNull(descriptor);
			return this;
		}

		/**
		 * Sets the memory properties for this image.
		 * @param props Memory properties
		 */
		public Builder properties(MemoryProperties<VkImageUsageFlag> props) {
			this.props = notNull(props);
			return this;
		}

		/**
		 * Adds an image creation flag.
		 * @param flag Image creation flag
		 */
		public Builder flag(VkImageCreateFlag flag) {
			flags.add(notNull(flag));
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
			this.samples = IntegerEnumeration.reverse(VkSampleCount.class).map(samples);
			return this;
		}

		/**
		 * Sets the image tiling arrangement (default is {@link VkImageTiling#TILING_OPTIMAL}).
		 * @param tiling Tiling arrangement
		 */
		public Builder tiling(VkImageTiling tiling) {
			this.tiling = notNull(tiling);
			return this;
		}

		/**
		 * Sets the initial image layout (default is {@link VkImageLayout#UNDEFINED}).
		 * @param layout Initial layout
		 */
		public Builder initialLayout(VkImageLayout layout) {
			switch(layout) {
				case UNDEFINED, PREINITIALIZED -> this.layout = layout;
				default -> throw new IllegalArgumentException("Invalid initial layout: " + layout);
			}
			return this;
		}

		/**
		 * Constructs this image.
		 * @param dev 			Logical device
		 * @param allocator		Memory allocator
		 * @return New image
		 * @see DefaultImage#DefaultImage(Pointer, DeviceContext, Descriptor, DeviceMemory)
		 */
		public DefaultImage build(DeviceContext dev, AllocationService allocator) {
			// Validate
			if(descriptor == null) throw new IllegalArgumentException("No image descriptor specified");
			if(props == null) throw new IllegalArgumentException("No memory properties specified");

			// Populate image structure
			final var info = new VkImageCreateInfo();
			info.flags = IntegerEnumeration.reduce(flags);
			info.imageType = descriptor.type();
			info.format = descriptor.format();
			info.extent = descriptor.extents().toExtent();
			info.mipLevels = descriptor.levelCount();
			info.arrayLayers = descriptor.layerCount();
			info.samples = samples;
			info.tiling = tiling;
			info.initialLayout = layout;
			info.usage = IntegerEnumeration.reduce(props.usage());
			info.sharingMode = props.mode();
			// TODO - queueFamilyIndexCount, pQueueFamilyIndices

			// Allocate image
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
			check(lib.vkCreateImage(dev, info, null, handle));

			// Retrieve image memory requirements
			final var reqs = new VkMemoryRequirements();
			lib.vkGetImageMemoryRequirements(dev, handle.getValue(), reqs);

			// Allocate image memory
			final DeviceMemory mem = allocator.allocate(reqs, props);

			// Bind memory to image
			check(lib.vkBindImageMemory(dev, handle.getValue(), mem, 0));

			// Create image
			return new DefaultImage(handle.getValue(), dev, descriptor, mem);
		}
	}
}
