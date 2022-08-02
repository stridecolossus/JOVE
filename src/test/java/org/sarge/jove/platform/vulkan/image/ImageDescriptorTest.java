package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ImageDescriptorTest extends AbstractVulkanTest {
	private static final VkImageType TYPE = VkImageType.TWO_D;
	private static final Set<VkImageAspect> ASPECTS = Set.of(VkImageAspect.COLOR);
	private static final Extents EXTENTS = new Extents(new Dimensions(3, 4));

	private ImageDescriptor descriptor;

	@BeforeEach
	void before() {
		descriptor = new ImageDescriptor(TYPE, FORMAT, EXTENTS, ASPECTS, 2, 3);
	}

	@Test
	void constructor() {
		assertEquals(TYPE, descriptor.type());
		assertEquals(FORMAT, descriptor.format());
		assertEquals(EXTENTS, descriptor.extents());
		assertEquals(ASPECTS, descriptor.aspects());
		assertEquals(0, descriptor.mipLevel());
		assertEquals(0, descriptor.baseArrayLayer());
		assertEquals(2, descriptor.levelCount());
		assertEquals(3, descriptor.layerCount());
	}

	@DisplayName("An image must have at least one aspect")
	@Test
	void emptyAspects() {
		assertThrows(IllegalArgumentException.class, () -> new ImageDescriptor(TYPE, FORMAT, EXTENTS, Set.of(), 1, 1));
	}

	@DisplayName("The image aspects must be a valid combination")
	@Test
	void invalidAspects() {
		final var aspects = Set.of(VkImageAspect.COLOR, VkImageAspect.DEPTH);
		assertThrows(IllegalArgumentException.class, "Invalid image aspects", () -> new ImageDescriptor(TYPE, FORMAT, EXTENTS, aspects, 1, 1));
	}

	@DisplayName("A 2D image must have a depth of one")
	@Test
	void invalidExtentsDepth() {
		final Extents extents = new Extents(new Dimensions(2, 3), 4);
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(TYPE, FORMAT, extents, ASPECTS, 1, 1));
	}

	@DisplayName("A 1D image must have height and depth of one")
	@Test
	void invalidExtentsHeightDepth() {
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(VkImageType.ONE_D, FORMAT, EXTENTS, ASPECTS, 1, 1));
	}

	@DisplayName("A 3D image can only contain a single array layer")
	@Test
	void invalidArrayLayers() {
		assertThrows(IllegalArgumentException.class, "Array layers must be one", () -> new ImageDescriptor(VkImageType.THREE_D, FORMAT, EXTENTS, ASPECTS, 1, 2));
	}

	@DisplayName("The extents of an image...")
	@Nested
	class ExtentTests {
		private Extents extents;

		@BeforeEach
		void before() {
			extents = new Extents(new Dimensions(2, 4), 3);
		}

		@DisplayName("comprise the image dimensions and pixel depth")
		@Test
		void constructor() {
			assertEquals(new Dimensions(2, 4), extents.size());
			assertEquals(3, extents.depth());
		}

		@DisplayName("can be converted to Vulkan extent")
		@Test
		void extents() {
			final VkExtent3D result = extents.toExtent();
			assertNotNull(result);
			assertEquals(2, result.width);
			assertEquals(4, result.height);
			assertEquals(3, result.depth);
		}

		@DisplayName("can be converted to Vulkan offset")
		@Test
		void offset() {
			final VkOffset3D result = extents.toOffset();
			assertNotNull(result);
			assertEquals(2, result.x);
			assertEquals(4, result.y);
			assertEquals(3, result.z);
		}

		@DisplayName("can be scaled to a given MIP level")
		@Test
		void mip() {
			assertEquals(extents, extents.mip(0));
			assertEquals(new Extents(new Dimensions(1, 2), 3), extents.mip(1));
		}
	}

	@Test
	void builder() {
		final ImageDescriptor result = new ImageDescriptor.Builder()
				.type(TYPE)
				.format(FORMAT)
				.extents(EXTENTS)
				.aspect(VkImageAspect.COLOR)
				.mipLevels(2)
				.arrayLayers(3)
				.build();

		assertEquals(descriptor, result);
	}
}
