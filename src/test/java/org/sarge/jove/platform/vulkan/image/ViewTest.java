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
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.TransientNativeObject;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.platform.vulkan.VkComponentSwizzle;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageViewCreateInfo;
import org.sarge.jove.platform.vulkan.VkImageViewType;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

import com.sun.jna.Pointer;

public class ViewTest extends AbstractVulkanTest {
	private interface MockImage extends Image, TransientNativeObject {
	}

	private View view;
	private MockImage image;
	private ClearValue clear;

	@BeforeEach
	void before() {
		// Create image descriptor
		final ImageDescriptor descriptor = new ImageDescriptor.Builder()
				.format(FORMAT)
				.extents(new Extents(new Dimensions(3, 4)))
				.aspect(VkImageAspect.COLOR)
				.build();

		// Create image
		image = mock(MockImage.class);
		when(image.descriptor()).thenReturn(descriptor);

		// Create image view
		view = new View(new Pointer(1), image, dev);

		// Init clear value
		clear = new ColourClearValue(Colour.WHITE);
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
		view.clear(clear);
		assertEquals(clear, view.clear());
	}

	@Test
	void clearNone() {
		view.clear(ClearValue.NONE);
	}

	@Test
	void clearInvalidAspect() {
		assertThrows(IllegalArgumentException.class, () -> view.clear(ClearValue.DEPTH));
	}

	@Test
	void destroy() {
		view.destroy();
		verify(lib).vkDestroyImageView(dev, view, null);
		verify(image).destroy();
		verifyNoMoreInteractions(lib);
	}

	@Test
	void of() {
		when(image.device()).thenReturn(dev);
		assertNotNull(View.of(image));
	}

	@Nested
	class BuilderTests {
		private View.Builder builder;

		@BeforeEach
		void before() {
			when(image.device()).thenReturn(dev);
			builder = new View.Builder(image);
		}

		@Test
		void build() {
			// Build view
			view = builder
					.clear(clear)
					.build();

			// TODO - mapping
			// TODO - subresource

			// Check view
			assertNotNull(view);
			assertNotNull(view.handle());
			assertEquals(image, view.image());
			assertEquals(clear, view.clear());

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
