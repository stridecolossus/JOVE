package org.sarge.jove.platform.vulkan.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageAspect;
import org.sarge.jove.platform.vulkan.image.SubResource.Builder;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;

public class SubResourceTest {
	private Descriptor descriptor;

	@BeforeEach
	void before() {
		descriptor = new Descriptor.Builder()
				.aspect(VkImageAspect.DEPTH)
				.aspect(VkImageAspect.STENCIL)
				.format(AbstractVulkanTest.FORMAT)
				.extents(new ImageExtents(4, 5))
				.mipLevels(2)
				.arrayLayers(3)
				.build();
	}

	@Test
	void constructor() {
		final SubResource res = new SubResource(descriptor, Set.of(VkImageAspect.DEPTH), 0, 1, 1, 2);
		assertEquals(descriptor, res.descriptor());
		assertEquals(Set.of(VkImageAspect.DEPTH), res.mask());
		assertEquals(0, res.mipLevel());
		assertEquals(1, res.levelCount());
		assertEquals(1, res.baseArrayLayer());
		assertEquals(2, res.layerCount());
	}

	@Test
	void constructorInvalidAspect() {
		assertThrows(IllegalArgumentException.class, () -> new SubResource(descriptor, Set.of(VkImageAspect.COLOR), 0, 1, 0, 1));
	}

	@Test
	void constructorInvalidMipLevels() {
		assertThrows(IllegalArgumentException.class, () -> new SubResource(descriptor, Set.of(VkImageAspect.DEPTH), 0, 3, 0, 1));
	}

	@Test
	void constructorInvalidArrayLayers() {
		assertThrows(IllegalArgumentException.class, () -> new SubResource(descriptor, Set.of(VkImageAspect.DEPTH), 0, 1, 0, 4));
	}

	@Test
	void of() {
		final SubResource res = SubResource.of(descriptor);
		assertEquals(descriptor, res.descriptor());
		assertEquals(Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL), res.mask());
		assertEquals(0, res.mipLevel());
		assertEquals(2, res.levelCount());
		assertEquals(0, res.baseArrayLayer());
		assertEquals(3, res.layerCount());
	}

	@Test
	void ofResource() {
		final SubResource res = SubResource.of(descriptor);
		assertEquals(res, SubResource.of(descriptor, null));
		assertEquals(res, SubResource.of(descriptor, res));
	}

	@Test
	void ofResourceInvalid() {
		final Descriptor other = new Descriptor.Builder()
				.aspect(VkImageAspect.COLOR)
				.format(AbstractVulkanTest.FORMAT)
				.extents(new ImageExtents(4, 5))
				.build();

		assertThrows(IllegalStateException.class, () -> SubResource.of(other, SubResource.of(descriptor)));
	}

	@Test
	void equals() {
		final SubResource res = SubResource.of(descriptor);
		assertEquals(true, res.equals(res));
		assertEquals(false, res.equals(null));
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder(descriptor);
		}

		@Test
		void build() {
			final SubResource res = builder.build();
			assertNotNull(res);
			assertEquals(descriptor, res.descriptor());
			assertEquals(Set.of(VkImageAspect.DEPTH, VkImageAspect.STENCIL), res.mask());
			assertEquals(0, res.mipLevel());
			assertEquals(2, res.levelCount());
			assertEquals(0, res.baseArrayLayer());
			assertEquals(3, res.layerCount());
		}

		@Test
		void removeAspect() {
			final SubResource res = builder.remove(VkImageAspect.STENCIL).build();
			assertNotNull(res);
			assertEquals(Set.of(VkImageAspect.DEPTH), res.mask());
		}

		@Test
		void removeAspectNotPresent() {
			assertThrows(IllegalArgumentException.class, () -> builder.remove(VkImageAspect.COLOR));
		}
	}
}
