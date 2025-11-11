package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.image.Sampler.AddressMode;
import org.sarge.jove.util.MathsUtility;

public class SamplerTest {
	private Sampler sampler;
	private LogicalDevice dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		sampler = new Sampler(new Handle(1), dev);
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
			view = new View(new Handle(2), dev, mock(Image.class));
			res = sampler.resource(view);
		}

		@Test
		void constructor() {
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, res.type());
		}

		@Test
		void build() {
			final var info = (VkDescriptorImageInfo) res.descriptor();
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
					.mipLodBias(MathsUtility.HALF)
					.anisotropy(4)
					.compare(VkCompareOp.GREATER)
					.unnormalizedCoordinates(true)
					.build(dev);

			// Init expected descriptor
			final var expected = new VkSamplerCreateInfo() {
				@Override
				public boolean equals(Object obj) {
					final var info = (VkSamplerCreateInfo) obj;
					assertNotNull(info);
					assertEquals(VkFilter.LINEAR, info.minFilter);
					assertEquals(VkFilter.NEAREST, info.magFilter);
					assertEquals(VkSamplerMipmapMode.NEAREST, info.mipmapMode);
					assertEquals(MathsUtility.HALF, info.mipLodBias);
					assertEquals(2f, info.minLod);
					assertEquals(3f, info.maxLod);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeU);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeV);
					assertEquals(VkSamplerAddressMode.CLAMP_TO_BORDER, info.addressModeW);
					assertEquals(VkBorderColor.FLOAT_TRANSPARENT_BLACK, info.borderColor);
					assertEquals(true, info.anisotropyEnable);
					assertEquals(4f, info.maxAnisotropy);
					assertEquals(true, info.compareEnable);
					assertEquals(VkCompareOp.GREATER, info.compareOp);
					assertEquals(true, info.unnormalizedCoordinates);
					return true;
				}
			};

			// Check API
			assertNotNull(sampler);
			verify(dev.library()).vkCreateSampler(dev, expected, null, dev.factory().pointer());
		}

		@Test
		void buildInvalidLOD() {
			builder.minLod(2).maxLod(1);
			assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
		}
	}
}
