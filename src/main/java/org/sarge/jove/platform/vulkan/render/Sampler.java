package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Arrays;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.Resource;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>sampler</i> is used to sample from a texture image.
 * @author Sarge
 */
public class Sampler extends AbstractVulkanObject {
//  TODO - public static final float VK_LOD_CLAMP_NONE = 1000.0f;

	/**
	 * Helper - Determines the number of mipmap levels for the given image dimensions.
	 * @param dim Image dimensions
	 * @return Number of mipmap levels
	 */
	public static int levels(Dimensions dim) {
		final float max = Math.max(dim.width(), dim.height());
		return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
	}

	/**
	 * Constructor.
	 * @param handle		Sampler handle
	 * @param dev			Logical device
	 */
	Sampler(Pointer handle, LogicalDevice dev) {
		super(handle, dev);
	}

	@Override
	protected Destructor<Sampler> destructor(VulkanLibrary lib) {
		return lib::vkDestroySampler;
	}

	/**
	 * The <i>wrapping policy</i> specifies how coordinates outside of the texture are handled.
	 */
	public enum Wrap {
		/**
		 * Coordinates are repeated.
		 */
		REPEAT,

		/**
		 * Coordinates are clamped to the edge of the texture.
		 */
		EDGE,

		/**
		 * Uses the specified border colour.
		 * @see Builder#border(VkBorderColor)
		 */
		BORDER;

		/**
		 * Maps this policy to the sampler addressing mode.
		 * @param mirror Whether coordinates are mirrored
		 * @return Address mode
		 */
		public VkSamplerAddressMode mode(boolean mirror) {
			return switch(this) {
				case REPEAT -> mirror ? VkSamplerAddressMode.MIRRORED_REPEAT : VkSamplerAddressMode.REPEAT;
				case EDGE -> mirror ? VkSamplerAddressMode.MIRROR_CLAMP_TO_EDGE : VkSamplerAddressMode.CLAMP_TO_EDGE;
				case BORDER -> VkSamplerAddressMode.CLAMP_TO_BORDER;
			};
		}
	}

	/**
	 * Creates a descriptor set resource for this sampler on the given view.
	 * @param view View
	 * @return Sampler resource
	 */
	public Resource resource(View view) {
		return new Resource() {
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
	 * Builder for a sampler.
	 */
	public static class Builder {
		private final LogicalDevice dev;

		// Filters
		private VkFilter magFilter = VkFilter.LINEAR;
		private VkFilter minFilter = VkFilter.LINEAR;

		// Mipmap settings
		private VkSamplerMipmapMode mipmapMode = VkSamplerMipmapMode.LINEAR;
		private float minLod;
		private float maxLod;
		private float mipLodBias; // = 1;

		// Wrapping settings
		private final VkSamplerAddressMode[] addressMode = new VkSamplerAddressMode[3];
		private VkBorderColor border;

		// Anisotropy settings
		private float anisotropy = 1f;

		// TODO
//		private boolean compareEnable;
//		private VkCompareOp compareOp;
//		private boolean unnormalizedCoordinates;
//		flags

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		public Builder(LogicalDevice dev) {
			this.dev = notNull(dev);
			Arrays.fill(addressMode, VkSamplerAddressMode.REPEAT);
		}

		/**
		 * Sets the magnification filter.
		 * @param min Magnification filter (default is {@link VkFilter#LINEAR})
		 */
		public Builder mag(VkFilter mag) {
			this.magFilter = notNull(mag);
			return this;
		}

		/**
		 * Sets the minification filter.
		 * @param min Minification filter (default is {@link VkFilter#LINEAR})
		 */
		public Builder min(VkFilter min) {
			this.minFilter = notNull(min);
			return this;
		}

		/**
		 * Sets the mipmap mode.
		 * @param mode Mipmap mode (default is {@link VkSamplerMipmapMode#LINEAR})
		 */
		public Builder mipmap(VkSamplerMipmapMode mode) {
			this.mipmapMode = notNull(mode);
			return this;
		}

		/**
		 * Sets the minimum LOD value.
		 * @param minLod Minimum LOD
		 */
		public Builder minLod(float minLod) {
			this.minLod = minLod;
			return this;
		}

		/**
		 * Sets the maximum LOD value.
		 * @param minLod Maximum LOD
		 */
		public Builder maxLod(float maxLod) {
			this.maxLod = maxLod;
			return this;
		}

		/**
		 * Sets the wrapping policy for the given component.
		 * @param component		Component index 0..2 (U, V or W direction)
		 * @param wrap			Wrapping policy
		 * @param mirror		Whether coordinates are mirrored
		 */
		public Builder wrap(int component, Wrap wrap, boolean mirror) {
			addressMode[component] = wrap.mode(mirror);
			return this;
		}

		/**
		 * Sets the wrapping policy for <b>all</b> components.
		 * @param wrap			Wrapping policy
		 * @param mirror		Whether coordinates are mirrored
		 */
		public Builder wrap(Wrap wrap, boolean mirror) {
			final VkSamplerAddressMode mode = wrap.mode(mirror);
			Arrays.fill(addressMode, mode);
			return this;
		}

		/**
		 * Sets the texture border colour (required for a wrapping policy of {@link Wrap#BORDER}).
		 * @param border Border colour
		 */
		public Builder border(VkBorderColor border) {
			this.border = notNull(border);
			return this;
		}

		/**
		 * Sets the number of texel samples for anisotropy filtering.
		 * @param maxAnisotropy Number of texel samples
		 */
		//@DeviceLimit(1, "maxSamplerAnisotropy")
		//@DeviceFeature("samplerAnisotropy")
		public Builder anisotropy(float anisotropy) {
			this.anisotropy = oneOrMore(anisotropy);
			return this;
		}

		/**
		 * Builds this sampler.
		 * @return New sampler
		 * @throws IllegalArgumentException if the minimum LOD is greater-than the maximum LOD
		 * @throws IllegalArgumentException if the border colour is not specified for a border wrapping policy (see {@link #border(VkBorderColor)})
		 */
		public Sampler build() {
			// Create descriptor
			final VkSamplerCreateInfo info = new VkSamplerCreateInfo();

			// Init filters
			info.magFilter = magFilter;
			info.minFilter = minFilter;

			// Init addressing modes
			if(border == null) {
				for(VkSamplerAddressMode mode : addressMode) {
					if(mode == VkSamplerAddressMode.CLAMP_TO_BORDER) {
						throw new IllegalArgumentException("Border colour must be specified for clamp-to-border address mode");
					}
				}
			}
			info.addressModeU = addressMode[0];
			info.addressModeV = addressMode[1];
			info.addressModeW = addressMode[2];
			info.borderColor = border;

			// Init mipmap settings
			if(maxLod < minLod) throw new IllegalArgumentException("Maximum LOD cannot exceed minimum LOD");
			info.mipmapMode = mipmapMode;
			info.mipLodBias = mipLodBias;
			info.minLod = minLod;
			info.maxLod = maxLod;

			// Init anisotrophy settings
			if(anisotropy > 1) {
				// TODO - invalid if cubic min/mag filter
				info.anisotropyEnable = VulkanBoolean.TRUE;
				info.maxAnisotropy = anisotropy;
			}

			// Init comparison operation
			// TODO
//			info.compareEnable = VulkanBoolean.FALSE;
			info.compareOp = VkCompareOp.ALWAYS;

			// Init other properties
			// TODO
//			info.unnormalizedCoordinates = VulkanBoolean.FALSE;

			// Allocate sampler
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateSampler(dev, info, null, handle));

			// Create sampler
			return new Sampler(handle.getValue(), dev);
		}
	}
}
