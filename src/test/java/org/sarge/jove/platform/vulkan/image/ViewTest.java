package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
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
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.TransientNativeObject;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.DepthClearValue;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ViewTest extends AbstractVulkanTest {
	private interface MockImage extends Image, TransientNativeObject {
	}

	private View view;
	private MockImage image;

	@BeforeEach
	void before() {
		// Create image descriptor
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.format(FORMAT)
				.extents(new ImageExtents(3, 4))
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = mock(MockImage.class);
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
		assertEquals(new ImageExtents(3, 4), view.extents());
	}

	@Test
	void clear() {
		final ClearValue clear = new ColourClearValue(Colour.WHITE);
		view.clear(clear);
		assertEquals(clear, view.clear());
	}

	@Test
	void clearNone() {
		view.clear(ClearValue.NONE);
	}

	@Test
	void clearInvalidAspect() {
		assertThrows(IllegalArgumentException.class, () -> view.clear(DepthClearValue.DEFAULT));
	}

	@Test
	void type() {
		assertEquals(VkImageViewType.VIEW_TYPE_1D, View.type(VkImageType.IMAGE_TYPE_1D));
		assertEquals(VkImageViewType.VIEW_TYPE_2D, View.type(VkImageType.IMAGE_TYPE_2D));
		assertEquals(VkImageViewType.VIEW_TYPE_3D, View.type(VkImageType.IMAGE_TYPE_3D));
	}

	@Test
	void destroy() {
		view.close();
		verify(lib).vkDestroyImageView(dev, view, null);
		verify(image).close();
		verifyNoMoreInteractions(lib);
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
			when(image.device()).thenReturn(dev);
			builder = new View.Builder();
		}

		@Test
		void build() {
			// Build view
			view = builder.build(image);

			// Check view
			assertNotNull(view);
			assertNotNull(view.handle());
			assertEquals(image, view.image());
			assertEquals(ClearValue.NONE, view.clear());

			// Check API
			final ArgumentCaptor<VkImageViewCreateInfo> captor = ArgumentCaptor.forClass(VkImageViewCreateInfo.class);
			verify(lib).vkCreateImageView(eq(dev), captor.capture(), isNull(), eq(POINTER));

			// Check create descriptor
			final VkImageViewCreateInfo info = captor.getValue();
			assertNotNull(info);
			assertEquals(image.handle(), info.image);
			assertEquals(VkImageViewType.VIEW_TYPE_2D, info.viewType);
			assertEquals(0, info.flags);
			assertEquals(FORMAT, info.format);

			// Check component mapping
			assertNotNull(info.components);
			assertEquals(VkComponentSwizzle.IDENTITY, info.components.r);
			assertEquals(VkComponentSwizzle.IDENTITY, info.components.g);
			assertEquals(VkComponentSwizzle.IDENTITY, info.components.b);
			assertEquals(VkComponentSwizzle.IDENTITY, info.components.a);

			// Check resource range
			assertNotNull(info.subresourceRange);
			assertEquals(VkImageAspect.COLOR.value(), info.subresourceRange.aspectMask);
			assertEquals(0, info.subresourceRange.baseMipLevel);
// TODO - remaining levels/layers
//			assertEquals(SubResourceBuilder.REMAINING, info.subresourceRange.levelCount);
			assertEquals(1, info.subresourceRange.levelCount);
			assertEquals(0, info.subresourceRange.baseArrayLayer);
//			assertEquals(SubResourceBuilder.REMAINING, info.subresourceRange.layerCount);
			assertEquals(1, info.subresourceRange.layerCount);
		}
	}
}
