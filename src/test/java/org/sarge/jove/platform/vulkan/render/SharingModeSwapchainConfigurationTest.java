package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.render.Swapchain.Builder;

class SharingModeSwapchainConfigurationTest {
	private static class MockSwapchainBuilder extends Swapchain.Builder {
		boolean concurrent;

		@Override
		public Builder concurrent(Collection<Family> families) {
			assertNotEquals(1, families.size());
			concurrent = true;
			return super.concurrent(families);
		}
	}

	private MockSwapchainBuilder builder;
	private Family family;

	@BeforeEach
	void before() {
		family = new Family(0, 1, Set.of());
		builder = new MockSwapchainBuilder();
	}

	@Test
	void concurrent() {
		final var another = new Family(1, 2, Set.of());
		final var configuration = new SharingModeSwapchainConfiguration(List.of(family, another));
		configuration.configure(builder, null);
		assertEquals(true, builder.concurrent);
	}

	@Test
	void exclusive() {
		final var configuration = new SharingModeSwapchainConfiguration(List.of(family, family));
		configuration.configure(builder, null);
		assertEquals(false, builder.concurrent);
	}
}
