package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor.Extents;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

@SuppressWarnings("static-method")
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
		final Extents extents = new Extents(new Dimensions(2, 3), 4);
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(TYPE, FORMAT, extents, ASPECTS, 1, 1));
	}

	@DisplayName("2D image must have height and depth of one")
	@Test
	void invalidExtentsHeightDepth() {
		assertThrows(IllegalArgumentException.class, "Invalid extents", () -> new ImageDescriptor(VkImageType.ONE_D, FORMAT, EXTENTS, ASPECTS, 1, 1));
	}

	@DisplayName("3D image can only have one array layer")
	@Test
	void invalidArrayLayers() {
		assertThrows(IllegalArgumentException.class, "Array layers must be one", () -> new ImageDescriptor(VkImageType.THREE_D, FORMAT, EXTENTS, ASPECTS, 1, 2));
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
