package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>sampler</i> is used to sample from a texture image.
 * @author Sarge
 */
public class Sampler extends AbstractVulkanObject {
	/**
	 * Constructor.
	 * @param handle		Sampler handle
	 * @param dev			Logical device
	 */
	Sampler(Pointer handle, DeviceContext dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Sampler> destructor(VulkanLibrary lib) {
		return lib::vkDestroySampler;
	}

	/**
	 * Creates a descriptor set resource for this sampler on the given view.
	 * @param view View
	 * @return Sampler resource
	 */
	public DescriptorResource resource(View view) {
		return new DescriptorResource() {
			@Override
			public VkDescriptorType type() {
				return VkDescriptorType.COMBINED_IMAGE_SAMPLER;
			}

			@Override
			public void populate(VkWriteDescriptorSet write) {
				final var info = new VkDescriptorImageInfo();
				info.imageLayout = VkImageLayout.SHADER_READ_ONLY_OPTIMAL;
				info.sampler = Sampler.this.handle();
				info.imageView = view.handle();
				write.pImageInfo = info;
			}
		};
	}

	/**
	 * The <i>address mode</i> specifies how coordinates outside of the texture are handled.
	 * @see VkSamplerAddressMode
	 */
	public enum AddressMode {
		/**
		 * Coordinates are repeated.
		 */
		REPEAT(VkSamplerAddressMode.REPEAT, VkSamplerAddressMode.MIRRORED_REPEAT),

		/**
		 * Coordinates are clamped to the edge of the texture.
		 */
		EDGE(VkSamplerAddressMode.CLAMP_TO_EDGE, VkSamplerAddressMode.MIRROR_CLAMP_TO_EDGE),

		/**
		 * Uses the specified border colour.
		 * @see Builder#border(VkBorderColor)
		 */
		BORDER(VkSamplerAddressMode.CLAMP_TO_BORDER, null);

		private final VkSamplerAddressMode mode, mirrored;

		private AddressMode(VkSamplerAddressMode mode, VkSamplerAddressMode mirrored) {
			this.mode = notNull(mode);
			this.mirrored = mirrored;
		}

		/**
		 * @return Address mode
		 */
		public VkSamplerAddressMode mode() {
			return mode;
		}

		/**
		 * @return Mirrored address mode
		 * @throws IllegalStateException if this mode cannot be mirrored
		 */
		public VkSamplerAddressMode mirror() {
			if(mirrored == null) throw new IllegalStateException("Address mode cannot be mirrored: " + this);
			return mirrored;
		}
	}

	/**
	 * Builder for a sampler.
	 */
	public static class Builder {
		/**
		 * Default maximum LOD clamp.
		 */
		private static final float VK_LOD_CLAMP_NONE = 1000;

		private final VkSamplerCreateInfo info = new VkSamplerCreateInfo();

		public Builder() {
			min(VkFilter.LINEAR);
			mag(VkFilter.LINEAR);
			mipmap(VkSamplerMipmapMode.LINEAR);
			maxLod(VK_LOD_CLAMP_NONE);
			anisotropy(1);
			mode(VkSamplerAddressMode.REPEAT);
			border(VkBorderColor.FLOAT_TRANSPARENT_BLACK);
			unnormalizedCoordinates(false);
		}

		/**
		 * Sets the magnification filter.
		 * @param min Magnification filter (default is {@link VkFilter#LINEAR})
		 */
		public Builder mag(VkFilter mag) {
			info.magFilter = notNull(mag);
			return this;
		}

		/**
		 * Sets the minification filter.
		 * @param min Minification filter (default is {@link VkFilter#LINEAR})
		 */
		public Builder min(VkFilter min) {
			info.minFilter = notNull(min);
			return this;
		}

		/**
		 * Sets the mipmap mode.
		 * @param mode Mipmap mode (default is {@link VkSamplerMipmapMode#LINEAR})
		 */
		public Builder mipmap(VkSamplerMipmapMode mode) {
			info.mipmapMode = notNull(mode);
			return this;
		}

		/**
		 * Sets the LOD bias to be added to the LOD calculation (default is zero).
		 * @param mipLodBias LOD bias
		 */
		public Builder mipLodBias(float mipLodBias) {
			info.mipLodBias = mipLodBias;
			return this;
		}
		// TODO - VkPhysicalDevicePortabilitySubsetFeaturesKHR::samplerMipLodBias

		/**
		 * Sets the minimum LOD value.
		 * @param minLod Minimum LOD
		 */
		public Builder minLod(float minLod) {
			info.minLod = zeroOrMore(minLod);
			return this;
		}

		/**
		 * Sets the maximum LOD value.
		 * @param minLod Maximum LOD
		 * @see Sampler#VK_LOD_CLAMP_NONE
		 */
		public Builder maxLod(float maxLod) {
			info.maxLod = zeroOrMore(maxLod);
			return this;
		}

		/**
		 * Sets the address mode for the given component.
		 * @param component			Component index 0..2 (U, V or W direction)
		 * @param mode				Address mode
		 * @throws IndexOutOfBoundsException for an invalid component index
		 * @see AddressingMode#mode(boolean)
		 */
		public Builder mode(int component, VkSamplerAddressMode mode) {
			Check.notNull(mode);
			switch(component) {
				case 0 -> info.addressModeU = mode;
				case 1 -> info.addressModeV = mode;
				case 2 -> info.addressModeW = mode;
				default -> throw new IndexOutOfBoundsException("Invalid address mode component: " + component);
			}
			return this;
		}

		/**
		 * Sets the address mode for all three components.
		 * @param mode Address mode
		 */
		public Builder mode(VkSamplerAddressMode mode) {
			for(int n = 0; n < 3; ++n) {
				mode(n, mode);
			}
			return this;
		}

		/**
		 * Sets the texture border colour.
		 * @param border Border colour
		 */
		public Builder border(VkBorderColor border) {
			info.borderColor = notNull(border);
			return this;
		}

		/**
		 * Sets the number of texel samples for anisotropy filtering (default is disabled).
		 * @param anisotropy Number of texel samples
		 */
		public Builder anisotropy(float anisotropy) {
			info.maxAnisotropy = oneOrMore(anisotropy);
			info.anisotropyEnable = VulkanBoolean.of(anisotropy > 1);
			return this;
		}

		/**
		 * Sets and enables the comparison operation.
		 * @param op Comparison operation
		 */
		public Builder compare(VkCompareOp op) {
			info.compareOp = notNull(op);
			info.compareEnable = VulkanBoolean.TRUE;
			return this;
		}

		/**
		 * Sets whether to use un-normalized texel coordinates (default is {@code false}).
		 * @param unnormalizedCoordinates Whether to use un-normalized coordinates
		 */
		public Builder unnormalizedCoordinates(boolean unnormalizedCoordinates) {
			info.unnormalizedCoordinates = VulkanBoolean.of(unnormalizedCoordinates);
			return this;
		}

		/**
		 * Builds this sampler.
		 * @param dev Logical device
		 * @return New sampler
		 * @throws IllegalArgumentException if the minimum LOD is greater-than the maximum LOD
		 */
		public Sampler build(DeviceContext dev) {
			// Validate
			if(info.minLod > info.maxLod) {
				throw new IllegalArgumentException("Invalid min/max LOD");
			}

			// Instantiate sampler
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
			check(lib.vkCreateSampler(dev, info, null, handle));

			// Create domain object
			return new Sampler(handle.getValue(), dev);
		}
	}

	/**
	 * Sampler API.
	 */
	interface Library {
		/**
		 * Creates an image sampler.
		 * @param device			Logical device
		 * @param pCreateInfo		Sampler descriptor
		 * @param pAllocator		Allocator
		 * @param pSampler			Returned sampler
		 * @return Result
		 */
		int vkCreateSampler(DeviceContext device, VkSamplerCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSampler);

		/**
		 * Destroys a sampler.
		 * @param device			Logical device
		 * @param sampler			Sampler
		 * @param pAllocator		Allocator
		 */
		void vkDestroySampler(DeviceContext device, Sampler sampler, Pointer pAllocator);
	}
}
