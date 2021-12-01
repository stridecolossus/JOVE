package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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

	@Nested
	class ResourceTests {
		private DescriptorResource res;
		private View view;

		@BeforeEach
		void before() {
			view = mock(View.class);
			res = sampler.resource(view);
			when(view.handle()).thenReturn(new Handle(new Pointer(2)));
		}

		@Test
		void constructor() {
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, res.type());
		}

		@Test
		void populate() {
			// Populate write descriptor
			final var write = new VkWriteDescriptorSet();
			res.populate(write);

			// Check descriptor
			final VkDescriptorImageInfo info = write.pImageInfo;
			assertNotNull(info);
			assertEquals(sampler.handle(), info.sampler);
			assertEquals(view.handle(), info.imageView);
			assertEquals(VkImageLayout.SHADER_READ_ONLY_OPTIMAL, info.imageLayout);
		}
	}

	@Nested
	class BuilderTests {
		private Sampler.Builder builder;

		@BeforeEach
		void before() {
			builder = new Sampler.Builder();
		}

		@Test
		void build() {
			// Create sampler
			final Sampler sampler = builder
					.min(VkFilter.LINEAR)
					.mag(VkFilter.NEAREST)
					.mipmap(VkSamplerMipmapMode.NEAREST)
					.wrap(VkSamplerAddressMode.CLAMP_TO_BORDER)
					.border(VkBorderColor.FLOAT_TRANSPARENT_BLACK)
					.minLod(2)
					.maxLod(3)
					.anisotropy(4)
					.build(dev);

			// Init expected descriptor
			final VkSamplerCreateInfo expected = new VkSamplerCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final VkSamplerCreateInfo info = (VkSamplerCreateInfo) obj;
					assertNotNull(info);
					assertEquals(VkFilter.LINEAR, info.minFilter);
					assertEquals(VkFilter.NEAREST, info.magFilter);
					assertEquals(VkSamplerMipmapMode.NEAREST, info.mipmapMode);
					assertEquals(0f, info.mipLodBias);
					assertEquals(2f, info.minLod);
					assertEquals(3f, info.maxLod);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeU);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeV);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeW);
					assertEquals(VkBorderColor.FLOAT_TRANSPARENT_BLACK, info.borderColor);
					assertEquals(VulkanBoolean.TRUE, info.anisotropyEnable);
					assertEquals(4f, info.maxAnisotropy);
					return true;
				}
			};

			// Check sampler
			assertNotNull(sampler);
			verify(lib).vkCreateSampler(dev, expected, null, POINTER);
		}

		@Test
		void buildDefaults() {
			assertNotNull(builder.build(dev));
		}

		@Test
		void buildInvalidLOD() {
			builder.minLod(2).maxLod(1);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}
}
