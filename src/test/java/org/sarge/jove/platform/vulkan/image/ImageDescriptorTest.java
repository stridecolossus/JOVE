package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.VkImageType;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class ImageDescriptorTest extends AbstractVulkanTest {
	private static final VkImageType TYPE = VkImageType.IMAGE_TYPE_2D;
	private static final Set<VkImageAspect> ASPECTS = Set.of(VkImageAspect.COLOR);
	private static final ImageExtents EXTENTS = new ImageExtents(3, 4);

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
		assertEquals(2, descriptor.levelCount());
		assertEquals(0, descriptor.baseArrayLayer());
		assertEquals(3, descriptor.layerCount());
	}

	@DisplayName("Image must have at least one aspect")
	@Test
	void emptyAspects() {
		assertThrows(IllegalArgumentException.class, () -> new ImageDescriptor(TYPE, FORMAT, EXTENTS, Set.of(), 1, 1));
	}

	@DisplayName("Image aspects must be a valid combination")
	@Test
	void invalidAspects() {
		final var aspects = Set.of(VkImageAspect.COLOR, VkImageAspect.DEPTH);
		assertThrows(IllegalArgumentException.class, "Invalid image aspects", () -> new ImageDescriptor(TYPE, FORMAT, EXTENTS, aspects, 1, 1));
	}

	@DisplayName("2D image must have depth of one")
	@Test
	void invalidExtentsDepth() {
		final ImageExtents extents = new ImageExtents(new Dimensions(2, 3), 4);
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(TYPE, FORMAT, extents, ASPECTS, 1, 1));
	}

	@DisplayName("2D image must have height and depth of one")
	@Test
	void invalidExtentsHeightDepth() {
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(VkImageType.IMAGE_TYPE_1D, FORMAT, EXTENTS, ASPECTS, 1, 1));
	}

	@DisplayName("3D image can only have one array layer")
	@Test
	void invalidArrayLayers() {
		assertThrows(IllegalArgumentException.class, "Array layers must be one", () -> new ImageDescriptor(VkImageType.IMAGE_TYPE_3D, FORMAT, EXTENTS, ASPECTS, 1, 2));
	}

	@Nested
	class BuilderTests {
		private ImageDescriptor.Builder builder;

		@BeforeEach
		void before() {
			builder = new ImageDescriptor.Builder();
		}

		@Test
		void build() {
			builder
				.type(TYPE)
				.format(FORMAT)
				.extents(EXTENTS)
				.aspect(VkImageAspect.COLOR)
				.mipLevels(2)
				.arrayLayers(3);

			assertEquals(descriptor, builder.build());
		}
	}
}
