package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.pipeline.Sampler.Wrap;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.Resource;

import com.sun.jna.Pointer;

public class SamplerTest extends AbstractVulkanTest {
	private Sampler sampler;

	@BeforeEach
	void before() {
		sampler = new Sampler(new Pointer(1), dev);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), sampler.handle());
	}

	@Test
	void levels() {
		assertEquals(1, Sampler.levels(new Dimensions(1, 1)));
		assertEquals(2, Sampler.levels(new Dimensions(2, 2)));
		assertEquals(2, Sampler.levels(new Dimensions(3, 3)));
		assertEquals(3, Sampler.levels(new Dimensions(4, 4)));
	}

	@Nested
	class ResourceTests {
		private VkDescriptorImageInfo info;
		private Resource<VkDescriptorImageInfo> res;
		private View view;

		@BeforeEach
		void before() {
			info = new VkDescriptorImageInfo();
			view = mock(View.class);
			res = sampler.resource(view);
			when(view.handle()).thenReturn(new Handle(new Pointer(2)));
		}

		@Test
		void constructor() {
			assertEquals(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, res.type());
		}

		@Test
		void identity() {
			final var identity = res.identity();
			assertNotNull(identity);
			assertNotNull(identity.get());
			assertEquals(VkDescriptorImageInfo.class, identity.get().getClass());
		}

		@Test
		void populate() {
			res.populate(info);
			assertEquals(sampler.handle(), info.sampler);
			assertEquals(view.handle(), info.imageView);
			assertEquals(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, info.imageLayout);
		}

		@Test
		void update() {
			final var write = new VkWriteDescriptorSet();
			res.apply(info, write);
			assertEquals(info, write.pImageInfo);
		}
	}

	@Nested
	class BuilderTests {
		private Sampler.Builder builder;

		@BeforeEach
		void before() {
			builder = new Sampler.Builder(dev);
		}

		@Test
		void build() {
			// Init required features
			final DeviceFeatures features = dev.features();
			when(features.isSupported("samplerAnisotropy")).thenReturn(true);

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
	//		assertEquals(1, info.mipLodBias);
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
	}
}
