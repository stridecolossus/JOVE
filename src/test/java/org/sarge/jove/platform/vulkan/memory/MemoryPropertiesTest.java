package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sarge.jove.platform.vulkan.VkMemoryProperty.HOST_CACHED;
import static org.sarge.jove.platform.vulkan.VkMemoryProperty.HOST_VISIBLE;
import static org.sarge.jove.platform.vulkan.VkSharingMode.CONCURRENT;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkImageUsageFlag;
import org.sarge.jove.platform.vulkan.VkMemoryProperty;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties.Builder;

public class MemoryPropertiesTest {
	private static final Set<VkImageUsageFlag> USAGE = Set.of(VkImageUsageFlag.COLOR_ATTACHMENT);
	private static final Set<VkMemoryProperty> REQUIRED = Set.of(HOST_VISIBLE);
	private static final Set<VkMemoryProperty> OPTIMAL = Set.of(HOST_VISIBLE, HOST_CACHED);

	private MemoryProperties props;

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
	void invalidUsageEmpty() {
		assertThrows(IllegalArgumentException.class, () -> new MemoryProperties(Set.of(), CONCURRENT, REQUIRED, OPTIMAL));
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
					.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
					.mode(CONCURRENT)
					.required(HOST_VISIBLE)
					.optimal()
					.optimal(HOST_CACHED)
					.build();

			assertEquals(props, result);
		}
	}
}
