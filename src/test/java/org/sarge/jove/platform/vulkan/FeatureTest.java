package org.sarge.jove.platform.vulkan;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Feature.AbstractBuilder;
import org.sarge.jove.platform.vulkan.Feature.Extension;
import org.sarge.jove.platform.vulkan.Feature.FeatureSet;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.platform.vulkan.Feature.ValidationLayer;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;

public class FeatureTest {
	@Nested
	class ExtensionTests {
		private Extension ext;

		@BeforeEach
		public void before() {
			ext = new Extension("ext");
		}

		@Test
		public void constructor() {
			assertEquals("ext", ext.name());
		}

		@Test
		public void matches() {
			assertEquals(true, ext.matches(ext));
		}

		@Test
		public void equals() {
			assertEquals(true, ext.equals(ext));
			assertEquals(true, ext.equals(new Extension("ext")));
			assertEquals(false, ext.equals(null));
			assertEquals(false, ext.equals(new Extension("other")));
		}
	}

	@Nested
	class LayerTests {
		private ValidationLayer layer;

		@BeforeEach
		public void before() {
			layer = new ValidationLayer ("layer", 2);
		}

		@Test
		public void constructor() {
			assertEquals("layer", layer.name());
			assertEquals(2, layer.version());
		}

		@Test
		public void matches() {
			assertEquals(true, layer.matches(new ValidationLayer("lower", 1)));
			assertEquals(true, layer.matches(layer));
			assertEquals(false, layer.matches(new ValidationLayer("higher", 3)));
		}

		@Test
		public void equals() {
			assertEquals(true, layer.equals(layer));
			assertEquals(true, layer.equals(new ValidationLayer("layer", 2)));
			assertEquals(false, layer.equals(null));
			assertEquals(false, layer.equals(new ValidationLayer("layer", 3)));
		}
	}

	@Nested
	class FeatureSetTests {
		private FeatureSet<ValidationLayer> set;
		private ValidationLayer layer, other;

		@BeforeEach
		public void before() {
			layer = new ValidationLayer("layer", 2);
			other = new ValidationLayer("other", 3);
			set = new FeatureSet<>(Set.of(layer));
		}

		@Test
		public void constructor() {
			assertEquals(Set.of(layer), set.features());
		}

		@Test
		public void duplicate() {
			assertThrows(IllegalArgumentException.class, () -> new FeatureSet<>(Set.of(new Extension("same"), new Extension("same"))));
		}

		@Test
		public void contains() {
			assertEquals(true, set.contains(layer));
			assertEquals(false, set.contains(other));
		}

		@Test
		public void containsByName() {
			assertEquals(true, set.contains("layer"));
			assertEquals(false, set.contains("other"));
		}

		@Test
		public void matches() {
			assertEquals(Set.of(), set.match(Set.of()));
			assertEquals(Set.of(), set.match(Set.of(layer)));
		}

		@Test
		public void matchNotPresent() {
			assertEquals(Set.of(other), set.match(Set.of(other)));
		}

		@Test
		public void matchLowerRequiredVersion() {
			final ValidationLayer lower = new ValidationLayer("layer", 1);
			assertEquals(Set.of(), set.match(Set.of(lower)));
		}

		@Test
		public void matchNotSupported() {
			final ValidationLayer higher = new ValidationLayer("layer", 3);
			assertEquals(Set.of(higher), set.match(Set.of(higher)));
		}

		@Test
		public void equals() {
			assertEquals(true, set.equals(set));
			assertEquals(true, set.equals(new FeatureSet<>(Set.of(layer))));
			assertEquals(false, set.equals(null));
			assertEquals(false, set.equals(new FeatureSet<>(Set.of())));
		}
	}

	@Nested
	class SupportedTests {
		private Supported supported;

		@BeforeEach
		public void before() {
			final VulkanFunction<VkExtensionProperties> extensions = (count, array) -> 0;
			final VulkanFunction<VkLayerProperties> layers = (count, array) -> 0;
			supported = new Supported(extensions, layers, ReferenceFactory.DEFAULT);
		}

		@Test
		public void constructor() {
			assertNotNull(supported.extensions());
			assertNotNull(supported.layers());
		}
	}

	@Nested
	class BuilderTests {
		class MockBuilder extends AbstractBuilder<MockBuilder> {
			private MockBuilder(Supported supported) {
				super(supported);
			}
		}

		private MockBuilder builder;
		private Supported supported;

		@BeforeEach
		public void before() {
			supported = mock(Supported.class);
			builder = new MockBuilder(supported);
			when(supported.extensions()).thenReturn(new FeatureSet<>(Set.of(new Extension("ext"))));
			when(supported.layers()).thenReturn(new FeatureSet<>(Set.of(new ValidationLayer("layer", 1))));
		}

		@Test
		public void extension() {
			builder.extension("ext");
			builder.validate();
			assertArrayEquals(new String[]{"ext"}, builder.extensions());
		}

		@Test
		public void extensionNotSupported() {
			builder.extension("cobblers");
			assertThrows(ServiceException.class, () -> builder.validate());
		}

		@Test
		public void layer() {
			builder.layer("layer", 1);
			builder.validate();
			assertArrayEquals(new String[]{"layer"}, builder.layers());
		}

		@Test
		public void layerNotSupported() {
			builder.layer("layer", 2);
			assertThrows(ServiceException.class, () -> builder.validate());
		}
	}
}
