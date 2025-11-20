package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.Sampler.AddressMode;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.util.EnumMask;

class SamplerTest {
	private static class MockSamplerLibrary extends MockVulkanLibrary {
		@Override
		public VkResult vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Handle pAllocator, Pointer pSampler) {
			assertNotNull(device);
			assertEquals(new EnumMask<>(), pCreateInfo.flags);
			assertEquals(VkFilter.LINEAR, pCreateInfo.minFilter);
			assertEquals(VkFilter.LINEAR, pCreateInfo.magFilter);
			assertEquals(VkSamplerMipmapMode.LINEAR, pCreateInfo.mipmapMode);
			assertEquals(0f, pCreateInfo.mipLodBias);
			assertEquals(0f, pCreateInfo.minLod);
			assertEquals(1000f, pCreateInfo.maxLod);
			assertEquals(1f, pCreateInfo.maxAnisotropy);
			assertEquals(VkSamplerAddressMode.REPEAT, pCreateInfo.addressModeU);
			assertEquals(VkSamplerAddressMode.REPEAT, pCreateInfo.addressModeV);
			assertEquals(VkSamplerAddressMode.REPEAT, pCreateInfo.addressModeW);
			assertEquals(VkBorderColor.FLOAT_TRANSPARENT_BLACK, pCreateInfo.borderColor);
			assertEquals(false, pCreateInfo.unnormalizedCoordinates);
			pSampler.set(new Handle(2));
			return VkResult.SUCCESS;
		}
	}

	private Sampler sampler;
	private LogicalDevice device;
	private MockSamplerLibrary library;

	@BeforeEach
	void before() {
		library = new MockSamplerLibrary();
		device = new MockLogicalDevice(library);
		sampler = new Sampler.Builder().build(device);
	}

	@Test
	void destroy() {
		sampler.destroy();
		assertEquals(true, sampler.isDestroyed());
	}

	@Nested
	class AddressModeTest {
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
	class ResourceTest {
		private DescriptorSet.Resource resource;
		private View view;

		@BeforeEach
		void before() {
			view = new View(new Handle(2), device, new MockImage());
			resource = sampler.resource(view);
		}

		@Test
		void constructor() {
			assertEquals(VkDescriptorType.COMBINED_IMAGE_SAMPLER, resource.type());
		}

		@Test
		void build() {
			final var info = (VkDescriptorImageInfo) resource.descriptor();
			assertEquals(sampler.handle(), info.sampler);
			assertEquals(view.handle(), info.imageView);
			assertEquals(VkImageLayout.SHADER_READ_ONLY_OPTIMAL, info.imageLayout);
		}
	}
}
