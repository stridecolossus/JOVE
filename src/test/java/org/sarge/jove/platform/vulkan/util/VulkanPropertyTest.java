package org.sarge.jove.platform.vulkan.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.util.VulkanProperty.Key;
import org.sarge.jove.platform.vulkan.util.VulkanProperty.Provider;

public class VulkanPropertyTest {
	private static final String NAME = "maxSamplerAnisotropy";
	private static final String FEATURE = "samplerAnisotropy";

	private VulkanProperty prop;

	@BeforeEach
	void before() {
		prop = new VulkanProperty(new Key(NAME, FEATURE), true, 4f);
	}

	@Test
	void constructor() {
		assertEquals(true, prop.isEnabled());
		assertEquals(Float.valueOf(4), prop.get());
	}

	@Test
	void validate() {
		prop.validate();
		prop.validate(1);
	}

	@Test
	void validateInvalidValue() {
		assertThrows(IllegalArgumentException.class, () -> prop.validate(5));
		assertThrows(IllegalArgumentException.class, () -> prop.validate(-1));
	}

	@Test
	void validateNotEnabled() {
		prop = new VulkanProperty(new Key(NAME), false, 1f);
		assertThrows(IllegalStateException.class, () -> prop.validate());
	}

	@Test
	void validateInvalidType() {
		prop = new VulkanProperty(new Key(NAME), true, true);
		assertThrows(UnsupportedOperationException.class, () -> prop.validate(1f));
	}

	@Nested
	class KeyTest {
		private Key key;

		@BeforeEach
		void before() {
			key = new Key(NAME, 0, null, FEATURE);
		}

		@Test
		void constructor() {
			assertEquals(NAME, key.name());
			assertEquals(0f, key.min());
			assertEquals(null, key.granularity());
			assertEquals(FEATURE, key.feature());
		}

		@Test
		void feature() {
			assertEquals(key, new Key(NAME, FEATURE));
		}

		@Test
		void build() {
			final var builder = new Key.Builder().name(NAME).feature(FEATURE);
			assertEquals(key, builder.build());
		}

		@Test
		void buildEmpty() {
			assertThrows(IllegalArgumentException.class, () -> new Key.Builder().build());
		}
	}

	@Nested
	class ProviderTest {
		private Provider provider;

		@BeforeEach
		void before() {
			// Init device limits
			final var limits = new VkPhysicalDeviceLimits();
			limits.maxSamplerAnisotropy = 4;
			limits.lineWidthRange = new float[]{1, 2};
			limits.lineWidthGranularity = 0.5f;
			limits.maxViewportDimensions = new int[]{3, 4};

			// Create provider
			provider = new Provider(limits, DeviceFeatures.of(Set.of(FEATURE)));
		}

		@Test
		void value() {
			assertEquals(prop, provider.property(new Key(NAME, FEATURE)));
		}

		@Test
		void simple() {
			provider.property(new Key(NAME)).validate();
		}

		@Test
		void range() {
			// Create floating-point range
			final VulkanProperty range = provider.property(new Key("lineWidthRange", 1, "lineWidthGranularity", null));
			assertNotNull(range);
			assertEquals(true, range.isEnabled());
			assertArrayEquals(new float[]{1f, 1.5f, 2f}, range.get());

			// Validate
			range.validate();
			range.validate(1f);
			range.validate(1.5f);
			range.validate(2f);

			// Check granularity
			assertThrows(IllegalArgumentException.class, () -> range.validate(1.25f));
		}

		@Test
		void integers() {
			final VulkanProperty range = provider.property(new Key("maxViewportDimensions", 0, null, null));
			assertNotNull(range);
			assertArrayEquals(new int[]{3, 4}, range.get());
			range.validate(3);
			range.validate(4);
			assertThrows(IllegalArgumentException.class, () -> range.validate(2));
			assertThrows(IllegalArgumentException.class, () -> range.validate(5));
		}
	}
}
