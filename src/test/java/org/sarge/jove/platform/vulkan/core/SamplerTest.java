package org.sarge.jove.platform.vulkan.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkBorderColor;
import org.sarge.jove.platform.vulkan.VkFilter;
import org.sarge.jove.platform.vulkan.VkSamplerAddressMode;
import org.sarge.jove.platform.vulkan.VkSamplerCreateInfo;
import org.sarge.jove.platform.vulkan.VkSamplerMipmapMode;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.Sampler.Wrap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class SamplerTest extends AbstractVulkanTest {
	private Sampler.Builder builder;

	@BeforeEach
	void before() {
		builder = new Sampler.Builder(dev);
	}

	@Test
	void build() {
		// Create sampler
		final Sampler sampler = builder
				.min(VkFilter.VK_FILTER_LINEAR)
				.mag(VkFilter.VK_FILTER_NEAREST)
				.mipmap(VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_NEAREST)
				.wrap(Wrap.BORDER, false)
				.border(VkBorderColor.VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK)
				.minLod(2)
				.maxLod(3)
				.anisotropy(4f)
				.build();

		// Check sampler
		assertNotNull(sampler);

		// Check API
		final ArgumentCaptor<VkSamplerCreateInfo> captor = ArgumentCaptor.forClass(VkSamplerCreateInfo.class);
		verify(lib).vkCreateSampler(eq(dev.handle()), captor.capture(), isNull(), eq(factory.ptr));

		// Check descriptor
		final VkSamplerCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(VkFilter.VK_FILTER_LINEAR, info.minFilter);
		assertEquals(VkFilter.VK_FILTER_NEAREST, info.magFilter);

		// Check mipmap settings
		assertEquals(VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_NEAREST, info.mipmapMode);
		assertEquals(1, info.mipLodBias);
		assertEquals(2, info.minLod);
		assertEquals(3, info.maxLod);

		// Check address modes
		assertEquals(VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER, info.addressModeU);
		assertEquals(VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER, info.addressModeV);
		assertEquals(VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER, info.addressModeW);
		assertEquals(VkBorderColor.VK_BORDER_COLOR_FLOAT_TRANSPARENT_BLACK, info.borderColor);

		// Check anisotropy settings
		assertEquals(VulkanBoolean.TRUE, info.anisotropyEnable);
		assertEquals(4f, info.maxAnisotropy);

		// TODO - others
	}

	@Test
	void buildDefaults() {
		assertNotNull(builder.build());
	}

	@Test
	void buildRequiresBorderColour() {
		builder.wrap(1, Wrap.BORDER, false);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void buildInvalidLOD() {
		builder.minLod(2).maxLod(1);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void levels() {
		assertEquals(1, Sampler.levels(new Dimensions(1, 1)));
		assertEquals(2, Sampler.levels(new Dimensions(2, 2)));
		assertEquals(2, Sampler.levels(new Dimensions(3, 3)));
		assertEquals(3, Sampler.levels(new Dimensions(4, 4)));
	}
}
