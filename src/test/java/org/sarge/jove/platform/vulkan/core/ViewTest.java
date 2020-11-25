package org.sarge.jove.platform.vulkan.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspectFlag;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.core.Image.DefaultImage;
import org.sarge.jove.platform.vulkan.core.Image.Descriptor.SubResourceBuilder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class ViewTest extends AbstractVulkanTest {
	private View view;
	private Image image;

	@BeforeEach
	void before() {
		// Create image descriptor
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.format(FORMAT)
				.extents(new Image.Extents(3, 4))
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
				.build();

		// Create image
		image = mock(Image.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create image view
		view = new View(new Pointer(1), image, dev);
	}

	@Test
	void constructor() {
		assertEquals(new Handle(new Pointer(1)), view.handle());
		assertEquals(dev, view.device());
		assertEquals(image, view.image());
		assertEquals(ClearValue.NONE, view.clear());
	}

	@Test
	void clear() {
		final ClearValue clear = new ColourClearValue(Colour.WHITE);
		view.clear(clear);
		assertEquals(clear, view.clear());
	}

	@Test
	void clearInvalidAspect() {
		assertThrows(IllegalArgumentException.class, () -> view.clear(ClearValue.DEPTH));
	}

	@Test
	void type() {
		assertEquals(VkImageViewType.VK_IMAGE_VIEW_TYPE_1D, View.type(VkImageType.VK_IMAGE_TYPE_1D));
		assertEquals(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D, View.type(VkImageType.VK_IMAGE_TYPE_2D));
		assertEquals(VkImageViewType.VK_IMAGE_VIEW_TYPE_3D, View.type(VkImageType.VK_IMAGE_TYPE_3D));
	}

	@Test
	void destroy() {
		view.destroy();
		verify(lib).vkDestroyImageView(dev.handle(), view.handle(), null);
		verifyNoMoreInteractions(lib);
	}

	@Test
	void destroyImage() {
		final DefaultImage def = mock(DefaultImage.class);
		view = new View(new Pointer(1), def, dev);
		view.destroy();
		verify(def).destroy();
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
			builder = new View.Builder(dev, image);
		}

		@Test
		void build() {
			// Build view
			final ClearValue clear = new ColourClearValue(Colour.WHITE);
			view = builder.clear(clear).build();

			// Check view
			assertNotNull(view);
			assertNotNull(view.handle());
			assertEquals(image, view.image());
			assertEquals(clear, view.clear());

			// Check API
			final ArgumentCaptor<VkImageViewCreateInfo> captor = ArgumentCaptor.forClass(VkImageViewCreateInfo.class);
			verify(lib).vkCreateImageView(eq(dev.handle()), captor.capture(), isNull(), isA(PointerByReference.class));

			// Check create descriptor
			final VkImageViewCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(image.handle(), info.image);
			assertEquals(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D, info.viewType);
			assertEquals(0, info.flags);
			assertEquals(FORMAT, info.format);

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
			assertEquals(SubResourceBuilder.REMAINING, info.subresourceRange.levelCount);
			assertEquals(0, info.subresourceRange.baseArrayLayer);
			assertEquals(SubResourceBuilder.REMAINING, info.subresourceRange.layerCount);
		}
	}
}
