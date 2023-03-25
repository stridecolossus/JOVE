package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Extents;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;

class ImageTest {
	private static final VkFormat FORMAT = VkFormat.R32G32B32A32_SFLOAT;

	@Test
	void cubemap() {
		assertEquals(6, Image.CUBEMAP_ARRAY_LAYERS);
	}

	@DisplayName("An image descriptor...")
	@Nested
	class DescriptorTests {
		private static final VkImageType TYPE = VkImageType.TWO_D;
		private static final Set<VkImageAspect> ASPECTS = Set.of(VkImageAspect.COLOR);
		private static final Extents EXTENTS = new Extents(new Dimensions(3, 4));

		private Descriptor descriptor;

		@BeforeEach
		void before() {
			descriptor = new Descriptor(TYPE, FORMAT, EXTENTS, ASPECTS, 2, 3);
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

		@DisplayName("for a 2D image must have a depth of one")
		@Test
		void invalidExtentsDepth() {
			final Extents extents = new Extents(new Dimensions(2, 3), 4);
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(TYPE, FORMAT, extents, ASPECTS, 1, 1));
		}

		@DisplayName("for a 1D image must have height and depth of one")
		@Test
		void invalidExtentsHeightDepth() {
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(VkImageType.ONE_D, FORMAT, EXTENTS, ASPECTS, 1, 1));
		}

		@DisplayName("for a 3D image can only contain a single array layer")
		@Test
		void invalidArrayLayers() {
			assertThrows(IllegalArgumentException.class, () -> new Descriptor(VkImageType.THREE_D, FORMAT, EXTENTS, ASPECTS, 1, 2));
		}

		@Nested
		class BuilderTests {
			private Descriptor.Builder builder;

			@BeforeEach
			void before() {
				builder = new Descriptor.Builder();
				builder.type(TYPE);
				builder.format(FORMAT);
				builder.extents(EXTENTS);
			}

    		@Test
    		void build() {
    			builder
    					.aspect(VkImageAspect.COLOR)
    					.mipLevels(2)
    					.arrayLayers(3)
    					.build();

    			assertEquals(descriptor, builder.build());
    		}

    		@ParameterizedTest
    		@EnumSource(value=VkImageAspect.class, names={"COLOR", "DEPTH", "STENCIL"})
    		void aspects(VkImageAspect aspect) {
    			builder.aspect(aspect).build();
    		}

    		@Test
    		void depthStencil() {
    			builder.aspect(VkImageAspect.DEPTH);
    			builder.aspect(VkImageAspect.STENCIL);
    			builder.build();
    		}

    		@Test
    		void empty() {
    			assertThrows(IllegalArgumentException.class, () -> builder.build());
    		}

    		@Test
    		void invalid() {
    			builder.aspect(VkImageAspect.COLOR);
    			builder.aspect(VkImageAspect.DEPTH);
    			assertThrows(IllegalArgumentException.class, () -> builder.build());
    		}
		}
	}
}
