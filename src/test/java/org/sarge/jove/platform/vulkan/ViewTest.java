package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.platform.IntegerEnumeration;

import com.sun.jna.ptr.PointerByReference;

public class ViewTest {
	private View view;
	private Image image;
	private VulkanLibrary lib;
	private LogicalDevice dev;

	@BeforeEach
	public void before() {
		// Create API
		lib = mock(VulkanLibrary.class);
		when(lib.factory()).thenReturn(new MockReferenceFactory());

		// Create device
		dev = mock(LogicalDevice.class);
		when(dev.library()).thenReturn(lib);

		// Create underlying image
		image = mock(Image.class);
		when(image.device()).thenReturn(dev);
		when(image.format()).thenReturn(VkFormat.VK_FORMAT_B8G8R8A8_UNORM);
		when(image.aspect()).thenReturn(Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));

		// Create image view
		view = new View.Builder().image(image).build();
	}

	@Test
	public void constructor() {
		assertNotNull(view.handle());
		assertEquals(image, view.image());
	}

	@Test
	public void create() {
		// Check view allocation
		final ArgumentCaptor<VkImageViewCreateInfo> captor = ArgumentCaptor.forClass(VkImageViewCreateInfo.class);
		verify(lib).vkCreateImageView(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

		// Check descriptor
		final VkImageViewCreateInfo info = captor.getValue();
		assertNotNull(info);
		assertEquals(image.handle(), info.image);
		assertEquals(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D, info.viewType);
		assertEquals(0, info.flags);
		assertEquals(VkFormat.VK_FORMAT_B8G8R8A8_UNORM, info.format);

		// Check component mapping
		assertNotNull(info.components);
		assertEquals(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY, info.components.r);
		assertEquals(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY, info.components.g);
		assertEquals(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY, info.components.b);
		assertEquals(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY, info.components.a);

		// Check resource range
		assertNotNull(info.subresourceRange);
		assertEquals(IntegerEnumeration.mask(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT), info.subresourceRange.aspectMask);
		assertEquals(0, info.subresourceRange.baseMipLevel);
		assertEquals(1, info.subresourceRange.levelCount);
		assertEquals(0, info.subresourceRange.baseArrayLayer);
		assertEquals(1, info.subresourceRange.layerCount);
	}

//	@Test
//	public void sampler() {
//		final Sampler.Descriptor descriptor = new Sampler.Descriptor.Builder().build();
//		final Sampler sampler = view.sampler(descriptor);
//		assertNotNull(sampler);
//		// TODO
//		//verify(library).vkCreateSampler(device, pCreateInfo, pAllocator, pSampler)
//	}

	@Test
	public void destroy() {
		view.destroy();
		verify(lib).vkDestroyImageView(dev.handle(), view.handle(), null);
	}
}
