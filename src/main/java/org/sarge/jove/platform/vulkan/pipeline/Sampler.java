package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.Arrays;
import java.util.function.Supplier;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.Resource;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>sampler</i> is used to sample from a texture image.
 * @author Sarge
 */
public class Sampler extends AbstractVulkanObject {
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
		super(handle, dev, dev.library()::vkDestroySampler);
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
				case REPEAT -> mirror ? VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT : VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
				case EDGE -> mirror ? VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_MIRROR_CLAMP_TO_EDGE : VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
				case BORDER -> VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
			};
		}
	}

	/**
	 * Creates a descriptor set resource for this sampler with the given texture image.
	 * @param view Texture image
	 * @return Sampler resource
	 */
	public Resource<VkDescriptorImageInfo> resource(View view) {
		return new Resource<>() {
			@Override
			public VkDescriptorType type() {
				return VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
			}

			@Override
			public Supplier<VkDescriptorImageInfo> identity() {
				return VkDescriptorImageInfo::new;
			}

			@Override
			public void populate(VkDescriptorImageInfo info) {
				info.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
				info.sampler = Sampler.this.handle();
				info.imageView = view.handle();
			}

			@Override
			public void apply(VkDescriptorImageInfo descriptor, VkWriteDescriptorSet write) {
				write.pImageInfo = descriptor;
			}
		};
	}

	/**
	 * Builder for a sampler.
	 */
	public static class Builder {
		private final LogicalDevice dev;

		// Filters
		private VkFilter magFilter = VkFilter.VK_FILTER_LINEAR;
		private VkFilter minFilter = VkFilter.VK_FILTER_LINEAR;

		// Mipmap settings
		private VkSamplerMipmapMode mipmapMode = VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR;
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
			Arrays.fill(addressMode, VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT);
		}

		/**
		 * Sets the magnification filter.
		 * @param min Magnification filter (default is {@link VkFilter#VK_FILTER_LINEAR})
		 */
		public Builder mag(VkFilter mag) {
			this.magFilter = notNull(mag);
			return this;
		}

		/**
		 * Sets the minification filter.
		 * @param min Minification filter (default is {@link VkFilter#VK_FILTER_LINEAR})
		 */
		public Builder min(VkFilter min) {
			this.minFilter = notNull(min);
			return this;
		}

		/**
		 * Sets the mipmap mode.
		 * @param mode Mipmap mode (default is {@link VkSamplerMipmapMode#VK_SAMPLER_MIPMAP_MODE_LINEAR})
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
		 * @throws IllegalStateException if anisotropy filtering is not enabled
		 */
		public Builder anisotropy(float anisotropy) {
			dev.features().check("samplerAnisotropy");
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

			// Init address mode
			if((border == null) && Arrays.stream(addressMode).anyMatch(VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER::equals)) {
				throw new IllegalArgumentException("Border colour not specified for addressing mode");
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
			info.compareOp = VkCompareOp.VK_COMPARE_OP_ALWAYS;

			// Init other properties
			// TODO
//			info.unnormalizedCoordinates = VulkanBoolean.FALSE;

			// Allocate sampler
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateSampler(dev.handle(), info, null, handle));

			// Create sampler
			return new Sampler(handle.getValue(), dev);
		}
	}
}
