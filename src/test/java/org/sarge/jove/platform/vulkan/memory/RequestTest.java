package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_CACHED_BIT;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_PROTECTED_BIT;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkMemoryRequirements;
import org.sarge.jove.platform.vulkan.memory.MemoryType.Heap;
import org.sarge.jove.platform.vulkan.memory.Request.Builder;

public class RequestTest {
	private static final int SIZE = 1;
	private static final int FILTER = 0b11;
	private static final Set<VkMemoryPropertyFlag> REQUIRED = Set.of(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
	private static final Set<VkMemoryPropertyFlag> OPTIMAL = Set.of(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_MEMORY_PROPERTY_HOST_CACHED_BIT);

	private Request req;
	private Heap heap;

	@BeforeEach
	void before() {
		req = new Request(SIZE, FILTER, REQUIRED, OPTIMAL);
		heap = new Heap(0, 0,Set.of());
	}

	@Test
	void constructor() {
		assertEquals(SIZE, req.size());
		assertEquals(FILTER, req.filter());
		assertEquals(REQUIRED, req.required());
		assertEquals(OPTIMAL, req.optimal());
	}

	@Test
	void invalidSize() {
		assertThrows(IllegalArgumentException.class, () -> new Request(0, FILTER, REQUIRED, OPTIMAL));
	}

	@Test
	void invalidFilter() {
		assertThrows(IllegalArgumentException.class, () -> new Request(SIZE, 0, REQUIRED, OPTIMAL));
	}

	@Nested
	class SelectTests {
		private MemoryType required, optimal;

		@BeforeEach
		void before() {
			required = new MemoryType(0, heap, REQUIRED);
			optimal = new MemoryType(1, heap, OPTIMAL);
		}

		@Test
		void select() {
			assertEquals(Optional.of(required), req.select(Set.of(required)));
		}

		@Test
		void optimal() {
			assertEquals(Optional.of(optimal), req.select(Set.of(required, optimal)));
		}

		@Test
		void none() {
			assertEquals(Optional.empty(), req.select(Set.of()));
		}

		@Test
		void noneMatched() {
			final MemoryType other = new MemoryType(2, heap, Set.of(VK_MEMORY_PROPERTY_PROTECTED_BIT));
			assertEquals(Optional.empty(), req.select(Set.of(other)));
		}

		@Test
		void filter() {
			req = new Request(SIZE, 0b100, REQUIRED, OPTIMAL);
			assertEquals(Optional.empty(), req.select(Set.of(required, optimal)));
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
			final Request result = builder
					.size(SIZE)
					.filter(FILTER)
					.required(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
					.optimal(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
					.optimal(VK_MEMORY_PROPERTY_HOST_CACHED_BIT)
					.build();

			assertEquals(req, result);
		}

		@Test
		void empty() {
			req = builder.size(SIZE).build();
			assertEquals(SIZE, req.size());
			assertEquals(Integer.MAX_VALUE, req.filter());
			assertEquals(Set.of(), req.required());
			assertEquals(Set.of(), req.optimal());
		}

		@Test
		void init() {
			// Populate memory requirements
			final VkMemoryRequirements struct = new VkMemoryRequirements();
			struct.size = SIZE;
			struct.memoryTypeBits = FILTER;

			// Build request from requirements
			req = builder.init(struct).build();
			assertEquals(SIZE, req.size());
			assertEquals(FILTER, req.filter());
		}
	}
}
