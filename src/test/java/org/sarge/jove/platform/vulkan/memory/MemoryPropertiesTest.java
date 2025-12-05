package org.sarge.jove.platform.vulkan.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.platform.vulkan.VkMemoryPropertyFlags.*;
import static org.sarge.jove.platform.vulkan.VkSharingMode.CONCURRENT;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlags;

public class MemoryPropertiesTest {
	private MemoryProperties<VkBufferUsageFlags> props;

	@BeforeEach
	void before() {
		props = new MemoryProperties<>(
				Set.of(VkBufferUsageFlags.UNIFORM_BUFFER),
				CONCURRENT,
				Set.of(HOST_VISIBLE, HOST_COHERENT),
				Set.of(DEVICE_LOCAL)
		);
	}

	@Test
	void constructor() {
		assertEquals(Set.of(VkBufferUsageFlags.UNIFORM_BUFFER), props.usage());
		assertEquals(CONCURRENT, props.mode());
		assertEquals(Set.of(HOST_VISIBLE, HOST_COHERENT), props.required());
		assertEquals(Set.of(DEVICE_LOCAL), props.optimal());
	}

	@Test
	void equals() {
		assertEquals(props, props);
		assertNotEquals(props, null);
	}

	@Nested
	class BuilderTests {
		private MemoryProperties.Builder<VkBufferUsageFlags> builder;

		@BeforeEach
		void before() {
			builder = new MemoryProperties.Builder<>();
		}

		@Test
		void build() {
			builder
					.usage(VkBufferUsageFlags.UNIFORM_BUFFER)
					.mode(CONCURRENT)
					.required(HOST_VISIBLE)
					.required(HOST_COHERENT)
					.optimal(DEVICE_LOCAL)
					.build();

			assertEquals(props, builder.build());
		}

		@DisplayName("The memory properties must contain at least one usage flag")
		@Test
		void usage() {
			builder.required(HOST_VISIBLE);
			assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
