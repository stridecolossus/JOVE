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

	private MemoryProperties props;
	private Heap heap;

	@BeforeEach
	void before() {
		props = new MemoryProperties<>(USAGE, CONCURRENT, REQUIRED, OPTIMAL);
		heap = new Heap(0, 0,Set.of());
	}

	@Test
	void constructor() {
		assertEquals(USAGE, props.usage());
		assertEquals(CONCURRENT, props.mode());
		assertEquals(REQUIRED, props.required());
		assertEquals(OPTIMAL, props.optimal());
	}

	@Test
	void invalidUsageEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new MemoryProperties(Set.of(), CONCURRENT, REQUIRED, OPTIMAL));
	}

	@Nested
	class SelectTests {
		private MemoryType required, optimal, empty;

		@BeforeEach
		void before() {
			required = new MemoryType(0, heap, REQUIRED);
			optimal = new MemoryType(1, heap, OPTIMAL);
			empty = new MemoryType(2, heap, Set.of());
		}

		@DisplayName("Required memory properties should be selected if the optimal set is not available")
		@Test
		void select() {
			assertEquals(Optional.of(required), props.select(Integer.MAX_VALUE, Set.of(required, empty)));
		}

		@DisplayName("Optimal memory properties should be selected when available")
		@Test
		void optimal() {
			assertEquals(Optional.of(optimal), props.select(Integer.MAX_VALUE, Set.of(required, optimal, empty)));
		}

		@DisplayName("No memory type should be selected if no matching properties are available")
		@Test
		void none() {
			assertEquals(Optional.empty(), props.select(Integer.MAX_VALUE, Set.of()));
			assertEquals(Optional.empty(), props.select(Integer.MAX_VALUE, Set.of(empty)));
		}

		@DisplayName("Memory types should be filtered by the request bit-mask")
		@Test
		void filter() {
			assertEquals(Optional.of(required), props.select(0b01, Set.of(required, optimal, empty)));
		}

		@DisplayName("A memory type with no properties should never be selected")
		@Test
		void empty() {
			props = new MemoryProperties<>(USAGE, CONCURRENT, Set.of(), Set.of());
			assertEquals(Optional.empty(), props.select(Integer.MAX_VALUE, Set.of(empty)));
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

			assertEquals(props, result);
		}
	}
}
