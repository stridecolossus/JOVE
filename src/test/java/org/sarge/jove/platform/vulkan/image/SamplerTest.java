package org.sarge.jove.platform.vulkan.image;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.image.Sampler.AddressMode;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.MathsUtil;

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
	class AddressModeTests {
		@Test
		void modes() {
			assertEquals(VkSamplerAddressMode.REPEAT, AddressMode.REPEAT.mode());
			assertEquals(VkSamplerAddressMode.CLAMP_TO_EDGE, AddressMode.EDGE.mode());
			assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, AddressMode.BORDER.mode());
		}

		@Test
		void mirrored() {
			assertEquals(VkSamplerAddressMode.MIRRORED_REPEAT, AddressMode.REPEAT.mirror());
			assertEquals(VkSamplerAddressMode.MIRROR_CLAMP_TO_EDGE, AddressMode.EDGE.mirror());
		}

		@Test
		void invalid() {
			assertThrows(IllegalStateException.class, () -> AddressMode.BORDER.mirror());
		}
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
			final var write = new VkWriteDescriptorSet();
			res.populate(write);

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
					.mode(VkSamplerAddressMode.CLAMP_TO_BORDER)
					.border(VkBorderColor.FLOAT_TRANSPARENT_BLACK)
					.minLod(2)
					.maxLod(3)
					.mipLodBias(MathsUtil.HALF)
					.anisotropy(4)
					.compare(VkCompareOp.GREATER)
					.unnormalizedCoordinates(true)
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
					assertEquals(MathsUtil.HALF, info.mipLodBias);
					assertEquals(2f, info.minLod);
					assertEquals(3f, info.maxLod);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeU);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeV);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeW);
					assertEquals(VkBorderColor.FLOAT_TRANSPARENT_BLACK, info.borderColor);
					assertEquals(VulkanBoolean.TRUE, info.anisotropyEnable);
					assertEquals(4f, info.maxAnisotropy);
					assertEquals(VulkanBoolean.TRUE, info.compareEnable);
					assertEquals(VkCompareOp.GREATER, info.compareOp);
					assertEquals(VulkanBoolean.TRUE, info.unnormalizedCoordinates);
					return true;
				}
			};

			// Check sampler
			assertNotNull(sampler);
			verify(lib).vkCreateSampler(dev, expected, null, factory.pointer());
		}

		@Test
		void buildDefaults() {
			final VkSamplerCreateInfo expected = new VkSamplerCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final VkSamplerCreateInfo info = (VkSamplerCreateInfo) obj;
					assertNotNull(info);
					assertEquals(VkFilter.LINEAR, info.minFilter);
					assertEquals(VkFilter.LINEAR, info.magFilter);
					assertEquals(VkSamplerMipmapMode.LINEAR, info.mipmapMode);
					assertEquals(0f, info.mipLodBias);
					assertEquals(0f, info.minLod);
					assertEquals(1000f, info.maxLod);
					assertEquals(VkSamplerAddressMode.REPEAT, info.addressModeU);
					assertEquals(VkSamplerAddressMode.REPEAT, info.addressModeV);
					assertEquals(VkSamplerAddressMode.REPEAT, info.addressModeW);
					assertEquals(VkBorderColor.FLOAT_TRANSPARENT_BLACK, info.borderColor);
					assertEquals(VulkanBoolean.FALSE, info.anisotropyEnable);
					assertEquals(1f, info.maxAnisotropy);
					assertEquals(null, info.compareEnable);
					assertEquals(null, info.compareOp);
					assertEquals(VulkanBoolean.FALSE, info.unnormalizedCoordinates);
					return true;
				}
			};
			assertNotNull(builder.build(dev));
			verify(lib).vkCreateSampler(dev, expected, null, factory.pointer());
		}

		@Test
		void buildInvalidLOD() {
			builder.minLod(2).maxLod(1);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}
}
