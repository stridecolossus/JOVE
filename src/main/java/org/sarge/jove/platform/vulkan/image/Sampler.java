package org.sarge.jove.platform.vulkan.image;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
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
	 * Default maximum LOD clamp.
	 */
	public static final float VK_LOD_CLAMP_NONE = 1000;

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
	 * Builder for a sampler.
	 */
	public static class Builder {
		private final VkSamplerCreateInfo info = new VkSamplerCreateInfo();

		public Builder() {
			min(VkFilter.LINEAR);
			mag(VkFilter.LINEAR);
			mipmap(VkSamplerMipmapMode.LINEAR);
			maxLod(VK_LOD_CLAMP_NONE);
			anisotropy(1);
			compare(VkCompareOp.NEVER);
			wrap(VkSamplerAddressMode.REPEAT);
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
		 * Sets the minimum LOD value.
		 * @param minLod Minimum LOD
		 */
		public Builder minLod(float minLod) {
			info.minLod = minLod;
			return this;
		}

		/**
		 * Sets the maximum LOD value.
		 * @param minLod Maximum LOD
		 * @see Sampler#VK_LOD_CLAMP_NONE
		 */
		public Builder maxLod(float maxLod) {
			info.maxLod = maxLod;
			return this;
		}

		/**
		 * Sets the wrapping policy for the given component.
		 * @param component			Component index 0..2 (U, V or W direction)
		 * @param mode				Addressing mode
		 * @throws IndexOutOfBoundsException for an invalid component index
		 * @see Wrap#mode(boolean)
		 */
		public Builder wrap(int component, VkSamplerAddressMode mode) {
			Check.notNull(mode);
			switch(component) {
				case 0: info.addressModeU = mode; break;
				case 1: info.addressModeV = mode; break;
				case 2: info.addressModeW = mode; break;
				default: throw new IndexOutOfBoundsException("Invalid address mode component: " + component);
			}
			return this;
		}

		/**
		 * Sets the wrapping policy for all three components.
		 * @param mode Addressing mode
		 */
		public Builder wrap(VkSamplerAddressMode mode) {
			for(int n = 0; n < 3; ++n) {
				wrap(n, mode);
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
		 * @param maxAnisotropy Number of texel samples
		 */
		public Builder anisotropy(float anisotropy) {
			info.maxAnisotropy = oneOrMore(anisotropy);
			info.anisotropyEnable = VulkanBoolean.TRUE;
			return this;
		}

		/**
		 * Sets the comparison operation (default is {@link VkCompareOp#NEVER}).
		 * @param op Comparison operation
		 */
		public Builder compare(VkCompareOp op) {
			info.compareOp = notNull(op);
			return this;
		}

		/**
		 * Builds this sampler.
		 * @param dev Logical device
		 * @return New sampler
		 * @throws IllegalArgumentException if the minimum LOD is greater-than the maximum LOD
		 */
		public Sampler build(LogicalDevice dev) {
			if(info.minLod > info.maxLod) throw new IllegalArgumentException("Invalid min/max LOD");
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = dev.factory().pointer();
			check(lib.vkCreateSampler(dev, info, null, handle));
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
		 * @param pSampler			Returned sampler handle
		 * @return Result code
		 */
		int vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSampler);

		/**
		 * Destroys a sampler.
		 * @param device			Logical device
		 * @param sampler			Sampler
		 * @param pAllocator		Allocator
		 */
		void vkDestroySampler(DeviceContext device, Sampler sampler, Pointer pAllocator);
	}
}