package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.HOST_CACHED;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.HOST_VISIBLE;
import static org.sarge.jove.platform.vulkan.VkSharingMode.CONCURRENT;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsage;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties.Builder;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;

public class MemoryPropertiesTest {
	private static final Set<VkImageUsage> USAGE = Set.of(VkImageUsage.COLOR_ATTACHMENT);
	private static final Set<VkMemoryPropertyFlag> REQUIRED = Set.of(HOST_VISIBLE);
	private static final Set<VkMemoryPropertyFlag> OPTIMAL = Set.of(HOST_VISIBLE, HOST_CACHED);

	private MemoryProperties req;
	private Heap heap;

	@BeforeEach
	void before() {
		req = new MemoryProperties<>(USAGE, CONCURRENT, REQUIRED, OPTIMAL);
		heap = new Heap(0, 0,Set.of());
	}

	@Test
	void constructor() {
		assertEquals(USAGE, req.usage());
		assertEquals(CONCURRENT, req.mode());
		assertEquals(REQUIRED, req.required());
		assertEquals(OPTIMAL, req.optimal());
	}

	@Test
	void invalidUsageEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new MemoryProperties(Set.of(), CONCURRENT, REQUIRED, OPTIMAL));
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
					.usage(VkImageUsage.COLOR_ATTACHMENT)
					.mode(CONCURRENT)
					.required(HOST_VISIBLE)
					.optimal(HOST_VISIBLE)
					.optimal(HOST_CACHED)
					.build();

			assertEquals(req, result);
		}
	}
}
