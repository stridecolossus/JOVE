package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkFormat;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class ViewTest extends AbstractVulkanTest {
	private View view;
	private Image image;

	@BeforeEach
	public void before() {
		image = mock(Image.class);
		view = new View(new Pointer(1), image, dev);
	}

	@Test
	public void constructor() {
		assertEquals(new Handle(new Pointer(1)), view.handle());
		assertEquals(dev, view.device());
		assertEquals(image, view.image());
	}

	@Test
	public void destroy() {
		final Handle handle = view.handle();
		view.destroy();
		verify(lib).vkDestroyImageView(dev.handle(), handle, null);
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
			builder = new View.Builder(dev);
		}

		@Test
		void build() {
			// Init image
			final Image.Descriptor descriptor = new Image.Descriptor.Builder()
					.format(VkFormat.VK_FORMAT_B8G8R8A8_UNORM)
					.extents(new Image.Extents(3, 4))
					.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
					.build();
			when(image.descriptor()).thenReturn(descriptor);

			// Build view
			view = builder
					.image(image)
					.subresource()
						.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
						.build()
					.build();

			// Check view
			assertNotNull(view);
			assertNotNull(view.handle());
			assertEquals(image, view.image());

			// Check API
			final ArgumentCaptor<VkImageViewCreateInfo> captor = ArgumentCaptor.forClass(VkImageViewCreateInfo.class);
			verify(lib).vkCreateImageView(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

			// Check create descriptor
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
			assertEquals(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value(), info.subresourceRange.aspectMask);
			assertEquals(0, info.subresourceRange.baseMipLevel);
			assertEquals(1, info.subresourceRange.levelCount);
			assertEquals(0, info.subresourceRange.baseArrayLayer);
			assertEquals(1, info.subresourceRange.layerCount);
		}

		@Test
		void buildRequiresImage() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
