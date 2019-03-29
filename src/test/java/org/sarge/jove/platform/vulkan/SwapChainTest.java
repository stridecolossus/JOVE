package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.SwapChain.Builder;

import com.sun.jna.Pointer;

public class SwapChainTest extends AbstractVulkanTest {
	@Nested
	class SwapChainTests {
		private SwapChain chain;
		private Dimensions extent;
		private ImageView view;

		@BeforeEach
		public void before() {
			extent = new Dimensions(640, 480);
			view = mock(ImageView.class);
			chain = new SwapChain(mock(Pointer.class), device, extent, List.of(view));
		}

		@Test
		public void constructor() {
			assertEquals(extent, chain.extent());
			assertEquals(List.of(view), chain.images());
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;
		private Surface surface;
		private VkSurfaceCapabilitiesKHR caps;

		@BeforeEach
		public void before() {
			// Init capabilities
			caps = new VkSurfaceCapabilitiesKHR();
			caps.minImageCount = 1;
			caps.maxImageCount = 2;

			// Init formats
			final VkSurfaceFormatKHR format = new VkSurfaceFormatKHR();
			format.format = VkFormat.VK_FORMAT_R32_SINT;
			format.colorSpace = VkColorSpaceKHR.VK_COLOR_SPACE_DOLBYVISION_EXT;

			// Create surface
			surface = mock(Surface.class);
			when(surface.capabilities()).thenReturn(caps);
			when(surface.formats()).thenReturn(List.of(format));

			// Create builder
			builder = new Builder(device, surface);
		}

		@Test
		public void build() {
			when(library.vkGetSwapchainImagesKHR(eq(device.handle()), any(Pointer.class), eq(factory.integer()), eq(factory.pointers(1)))).thenReturn(0);
			final SwapChain chain = builder
				.count(1)
				.format(VkFormat.VK_FORMAT_R32_SINT)
				.colour(VkColorSpaceKHR.VK_COLOR_SPACE_DOLBYVISION_EXT)
				// TODO - more
				.build();
			assertNotNull(chain);
		}

		@Test
		public void formatNotSupported() {
			assertThrows(IllegalArgumentException.class, () -> builder.format(VkFormat.VK_FORMAT_A1R5G5B5_UNORM_PACK16));
		}

		// TODO - others
	}
}
