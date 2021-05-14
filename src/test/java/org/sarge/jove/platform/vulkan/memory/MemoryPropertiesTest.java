package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.sarge.jove.platform.vulkan.VkSharingMode.VK_SHARING_MODE_CONCURRENT;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsageFlag;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties.Builder;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MemoryPropertiesTest {
	private static final Set<VkImageUsageFlag> USAGE = Set.of(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
	private static final Set<VkMemoryPropertyFlag> REQUIRED = Set.of(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
	private static final Set<VkMemoryPropertyFlag> OPTIMAL = Set.of(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_MEMORY_PROPERTY_HOST_CACHED_BIT);

	private MemoryProperties req;
	private Heap heap;

	@BeforeEach
	void before() {
		req = new MemoryProperties<>(USAGE, VK_SHARING_MODE_CONCURRENT, REQUIRED, OPTIMAL);
		heap = new Heap(0, 0,Set.of());
	}

	@Test
	void constructor() {
		assertEquals(USAGE, req.usage());
		assertEquals(VK_SHARING_MODE_CONCURRENT, req.mode());
		assertEquals(REQUIRED, req.required());
		assertEquals(OPTIMAL, req.optimal());
	}

	@Test
	void invalidUsageEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new MemoryProperties(Set.of(), VK_SHARING_MODE_CONCURRENT, REQUIRED, OPTIMAL));
	}

	@Nested
	class SelectTests {
		private MemoryType required, optimal, other;

		@BeforeEach
		void before() {
			required = new MemoryType(0, heap, REQUIRED);
			optimal = new MemoryType(1, heap, OPTIMAL);
			other = new MemoryType(2, heap, Set.of());
		}

		@DisplayName("Required memory properties should be selected if the optimal set is not available")
		@Test
		void select() {
			assertEquals(Optional.of(required), req.select(Integer.MAX_VALUE, Set.of(required, other)));
		}

		@DisplayName("Optimal memory properties should be selected when available")
		@Test
		void optimal() {
			assertEquals(Optional.of(optimal), req.select(Integer.MAX_VALUE, Set.of(required, optimal, other)));
		}

		@DisplayName("No memory type should be selected where the required properties are not available")
		@Test
		void none() {
			assertEquals(Optional.empty(), req.select(Integer.MAX_VALUE, Set.of()));
			assertEquals(Optional.empty(), req.select(Integer.MAX_VALUE, Set.of(other)));
		}

		@DisplayName("Memory types should be filtered by the request bit-mask")
		@Test
		void filter() {
			assertEquals(Optional.of(required), req.select(0b01, Set.of(required, optimal, other)));
		}
	}

	@Nested
	class BuilderTests {
		private Builder builder;

		@BeforeEach
		void before() {
			builder = new Builder();
		}

		@Test
		void build() {
			final MemoryProperties result = builder
					.usage(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
					.mode(VK_SHARING_MODE_CONCURRENT)
					.required(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
					.optimal(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
					.optimal(VK_MEMORY_PROPERTY_HOST_CACHED_BIT)
					.build();

			assertEquals(req, result);
		}
	}
}
