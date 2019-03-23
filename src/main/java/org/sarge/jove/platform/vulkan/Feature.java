package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;

import com.sun.jna.Native;

/**
 * A <i>feature</i> represents functionality supported by Vulkan.
 */
public abstract class Feature<T extends Feature<T>> extends AbstractEqualsObject {
	private final String name;

	/**
	 * Constructor.
	 * @param name Feature name
	 */
	protected Feature(String name) {
		this.name = notEmpty(name);
	}

	/**
	 * @return Feature name
	 */
	public String name() {
		return name;
	}

	/**
	 * @param feature Feature to match
	 * @return Whether this feature matches the given feature
	 */
	protected boolean matches(T feature) {
		return true;
	}

	/**
	 * Extension descriptor.
	 */
	public static final class Extension extends Feature<Extension> {
		/**
		 * Debug utility extension.
		 */
		public static final Extension DEBUG_UTILS = new Extension("VK_EXT_debug_utils");

		/**
		 * Swap-chain extension.
		 */
		public static final Extension SWAP_CHAIN = new Extension("VK_KHR_swapchain");

		/**
		 * Constructor.
		 * @param name Extension name
		 */
		public Extension(String name) {
			super(name);
		}
	}

	/**
	 * Validation layer descriptor.
	 */
	public static final class ValidationLayer extends Feature<ValidationLayer> {
		/**
		 * Standard validation layer.
		 */
		public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_LUNARG_standard_validation", 1);

		private final int ver;

		/**
		 * Constructor.
		 * @param name		Layer name
		 * @param ver		Implementation version
		 */
		public ValidationLayer(String name, int ver) {
			super(name);
			this.ver = zeroOrMore(ver);
		}

		/**
		 * @return Implementation version
		 */
		public int version() {
			return ver;
		}

		@Override
		protected boolean matches(ValidationLayer that) {
			return this.ver >= that.ver;
		}
	}

	/**
	 * A <i>feature set</i> is a collection of features.
	 * <p>
	 * The {@link FeatureSet#match(Set)} method can be used to test a set of <i>supported</i> features against a <i>required</i> set.
	 * <p>
	 * Example:
	 * <pre>
	 * // Determine required extensions
	 * final Set<Extension> required = ...
	 *
	 * // Retrieve supported extensions
	 * final FeatureSet<Extension> supported = ...
	 *
	 * // Check that the required extensions are supported
	 * final Set<Extension> missing = supported.match(required);
	 * if(!missing.isEmpty()) { ... }
	 * </pre>
	 * @param <T> Feature type
	 */
	public static class FeatureSet<T extends Feature<T>> extends AbstractEqualsObject {
		private final Map<String, T> features;

		/**
		 * Constructor.
		 * @param features Features
		 * @throws IllegalArgumentException for a duplicate feature name
		 */
		public FeatureSet(Set<T> features) {
			this.features = features.stream().collect(toMap(Feature::name, Function.identity()));
			if(this.features.size() != features.size()) throw new IllegalArgumentException("Feature set cannot contain duplicate name");
		}

		/**
		 * Constructor for an empty set.
		 */
		public FeatureSet() {
			this(Set.of());
		}

		/**
		 * @return Features in this set
		 */
		public Set<T> features() {
			return Set.copyOf(features.values());
		}

		/**
		 * Tests whether this set contains the given feature.
		 * @param feature Required feature
		 * @return Whether this set contains the given feature
		 */
		public boolean contains(T feature) {
			return features.containsValue(feature);
		}

		/**
		 * Tests for a feature by name.
		 * @param name Feature name
		 * @return Whether this set contains the given feature
		 */
		public boolean contains(String name) {
			return features.containsKey(name);
		}

		/**
		 * Matches this set of features against the given required features.
		 * @param required Required features
		 * @return Unsupported features
		 * @see Feature#matches(Feature)
		 */
		public Set<T> match(Set<T> required) {
			return required.stream().filter(Predicate.not(this::matches)).collect(toSet());
		}

		/**
		 * Tests whether this set contains a feature matching the given required feature.
		 * @param required Required feature
		 * @return Whether matched
		 */
		private boolean matches(T required) {
			final T supported = features.get(required.name());
			if(supported == null) {
				return false;
			}
			else {
				return supported.matches(required);
			}
		}
	}

	/**
	 * Convenience wrapper for supported extensions and layers.
	 */
	public static class Supported extends AbstractObject {
		private final FeatureSet<Extension> extensions;
		private final FeatureSet<ValidationLayer> layers;

		/**
		 * Constructor.
		 * @param extensions		Supported extensions
		 * @param layers			Supported layers
		 */
		public Supported(FeatureSet<Extension> extensions, FeatureSet<ValidationLayer> layers) {
			this.extensions = notNull(extensions);
			this.layers = notNull(layers);
		}

		/**
		 * Constructor given enumeration functions.
		 * @param extensions		Extensions function
		 * @param layers			Layers function
		 */
		public Supported(VulkanFunction<VkExtensionProperties> extensions, VulkanFunction<VkLayerProperties> layers) {
			this.extensions = enumerateExtensions(extensions);
			this.layers = enumerateLayers(layers);
		}

		/**
		 * @return Supported extensions
		 */
		public FeatureSet<Extension> extensions() {
			return extensions;
		}

		/**
		 * @return Supported layers
		 */
		public FeatureSet<ValidationLayer> layers() {
			return layers;
		}

		/**
		 * Enumerates supported extensions.
		 * @param func Extension function
		 * @return Supported extensions
		 */
		private static FeatureSet<Extension> enumerateExtensions(VulkanFunction<VkExtensionProperties> func) {
			final VkExtensionProperties[] array = VulkanFunction.enumerate(func, new VkExtensionProperties());
			final var extensions = Arrays.stream(array).map(e -> e.extensionName).map(Native::toString).map(Extension::new).collect(toSet());
			return new FeatureSet<>(extensions);
		}

		/**
		 * Enumerates supported validation layers.
		 * @param func Layer function
		 * @return Supported layers
		 */
		private static FeatureSet<ValidationLayer> enumerateLayers(VulkanFunction<VkLayerProperties> func) {
			final VkLayerProperties[] array = VulkanFunction.enumerate(func, new VkLayerProperties());
			final var layers = Arrays.stream(array).map(layer -> new ValidationLayer(Native.toString(layer.layerName), layer.implementationVersion)).collect(toSet());
			return new FeatureSet<>(layers);
		}
	}

	/**
	 * Base-class for a builder that uses supported/required extensions and layers.
	 * @see Supported
	 * @param <T> Implementing builder type used to fake the return type of the various methods in this builder
	 */
	public static abstract class AbstractBuilder<T extends AbstractBuilder<?>> {
		private final Supported supported;
		private final Set<Extension> requiredExtensions = new StrictSet<>();
		private final Set<ValidationLayer> requiredLayers = new StrictSet<>();

		/**
		 * Constructor.
		 * @param supported Supported extensions and layers
		 */
		protected AbstractBuilder(Supported supported) {
			this.supported = notNull(supported);
		}

		/**
		 * @return Required extension names
		 */
		public String[] extensions() {
			return toArray(requiredExtensions);
		}

		/**
		 * Adds required extensions.
		 * @param extensions Required extensions
		 */
		public T extension(FeatureSet<Extension> extensions) {
			this.requiredExtensions.addAll(extensions.features());
			return parent();
		}

		/**
		 * Adds a required extension.
		 * @param ext Extension
		 */
		public T extension(Extension ext) {
			requiredExtensions.add(ext);
			return parent();
		}

		/**
		 * Adds a required extension by name.
		 * @param ext Extension name
		 */
		public T extension(String ext) {
			return extension(new Extension(ext));
		}

		/**
		 * @return Required layer names
		 */
		public String[] layers() {
			return toArray(requiredLayers);
		}

		/**
		 * Adds required validation layers.
		 * @param layers Validation layers
		 */
		public T layers(FeatureSet<ValidationLayer> layers) {
			this.requiredLayers.addAll(layers.features());
			return parent();
		}

		/**
		 * Adds a required layer.
		 * @param layer 	Layer name
		 * @param ver		Version
		 */
		public T layer(String layer, int ver) {
			return layer(new ValidationLayer(layer, ver));
		}

		/**
		 * Adds a required layer.
		 * @param layer 	Layer name
		 * @param ver		Version
		 */
		public T layer(ValidationLayer layer) {
			requiredLayers.add(layer);
			return parent();
		}

		@SuppressWarnings("unchecked")
		private T parent() {
			return (T) this;
		}

		/**
		 * Converts a set of features to a string-array.
		 * @param features Features
		 * @return Feature names array
		 */
		private static String[] toArray(Set<? extends Feature<?>> features) {
			return features.stream().map(Feature::name).toArray(String[]::new);
		}

		/**
		 * Validates that <b>all</b> required extensions and layers are supported.
		 */
		protected void validate() {
			validate(supported.extensions(), requiredExtensions);
			validate(supported.layers(), requiredLayers);
		}

		/**
		 * Checks for unsupported features.
		 */
		private static <T extends Feature<T>> void validate(FeatureSet<T> supported, Set<T> required) {
			final var missing = supported.match(required);
			if(!missing.isEmpty()) {
				throw new ServiceException("Unsupported features: " + missing);
			}
		}
	}
}
