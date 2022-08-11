package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkMemoryProperty.*;
import static org.sarge.jove.platform.vulkan.VkSharingMode.CONCURRENT;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;

public class MemoryPropertiesTest {
	private static final Set<VkImageUsageFlag> USAGE = Set.of(VkImageUsageFlag.COLOR_ATTACHMENT);
	private static final Set<VkMemoryProperty> REQUIRED = Set.of(HOST_VISIBLE);
	private static final Set<VkMemoryProperty> OPTIMAL = Set.of(HOST_VISIBLE, HOST_CACHED);

	private MemoryProperties<VkImageUsageFlag> props;

	@BeforeEach
	void before() {
		props = new MemoryProperties<>(USAGE, CONCURRENT, REQUIRED, OPTIMAL);
	}

	@Test
	void constructor() {
		assertEquals(USAGE, props.usage());
		assertEquals(CONCURRENT, props.mode());
		assertEquals(REQUIRED, props.required());
		assertEquals(OPTIMAL, props.optimal());
	}

	@Test
	void equals() {
		assertEquals(props, props);
		assertNotEquals(props, null);
	}

	@Nested
	class BuilderTests {
		private MemoryProperties.Builder<VkImageUsageFlag> builder;

		@BeforeEach
		void before() {
			builder = new MemoryProperties.Builder<>();
		}

		@Test
		void build() {
			builder
					.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
					.mode(CONCURRENT)
					.required(HOST_VISIBLE)
					.optimal(HOST_VISIBLE)
					.optimal(HOST_CACHED)
					.build();

			assertEquals(props, builder.build());
		}

		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
