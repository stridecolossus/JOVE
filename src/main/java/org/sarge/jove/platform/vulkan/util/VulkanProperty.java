package org.sarge.jove.platform.vulkan.util;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>Vulkan property</i> is a convenience wrapper for a hardware limit and an associated optional device feature.
 * <p>
 * This intent of this helper class is to provide a more convenient alternative to programatically retrieving and validating compound properties from the {@link VkPhysicalDeviceLimits} structure.
 * <p>
 * A property {@link Key} specifies the structure field(s) comprising the property and an optional device feature name.
 * The {@link Provider} is used to retrieve properties for the local hardware.
 * <p>
 * The following field types are supported:
 * <p>
 * <table border=1>
 * <tr><th>type</th><th>example</th><th>notes</th></tr>
 * <tr><td>numeric</td><td>{@link VkPhysicalDeviceLimits#maxSamplerAnisotropy}</td><td>maximum value; {@code float}, {@code int} or a {@code long}</td></tr>
 * <tr><td>{@link VulkanBoolean}</td><td>{@link VkPhysicalDeviceLimits#strictLines}</td><td>cannot be validated</td></tr>
 * <tr><td>int[]</td><td>{@link VkPhysicalDeviceLimits#maxViewportDimensions}</td><td>range</td></tr>
 * <tr><td>float[]</td><td>{@link VkPhysicalDeviceLimits#lineWidthRange} and {@link VkPhysicalDeviceLimits#lineWidthGranularity}</td><td>either a <i>range</i> <b>or</b> a quantised array of possible values</td></tr>
 * </table>
 * <p>
 * Where <i>range</i> is a two-element array specifying a min/max range.
 * <p>
 * Usage:
 * <pre>
 * // Define property
 * Key key = new Key.Builder()
 *     .name("maxSamplerAnisotropy")
 *     .feature("samplerAnisotropy")
 *     .build();
 *
 * // Query property
 * DeviceContext dev = ...
 * VulkanProperty prop = dev.provider().property(key);
 *
 * // Retrieve the property value
 * float max = prop.get();
 *
 * // Validate the feature is enabled
 * prop.validate();
 *
 * // Validate an argument
 * prop.validate(8f);
 * </pre>
 * <p>
 * @author Sarge
 */
public class VulkanProperty {
	private final Key key;
	private final boolean enabled;
	private final Object value;

	/**
	 * Constructor.
	 * @param key			Property key
	 * @param enabled		Whether this property is enabled
	 * @param value			Property value
	 */
	VulkanProperty(Key key, boolean enabled, Object value) {
		this.key = notNull(key);
		this.enabled = enabled;
		this.value = notNull(value);
	}

	/**
	 * @return Whether this property is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Validates that this property is enabled and supports the given value.
	 * @param arg Property value
	 * @throws IllegalStateException if this property is not enabled
	 * @throws IllegalArgumentException if the given value is not supported by the hardware
	 * @throws UnsupportedOperationException if the property cannot be validated
	 * @see #validate()
	 */
	public void validate(float arg) {
		if(!enabled) throw new IllegalStateException("Property is not enabled: " + key);

		switch(value) {
			case Number num -> validate(arg, key.min, num);

			case float[] range -> {
				if(range.length == 2) {
					// Validate simple min/max range
					validate(arg, range[0], range[1]);
				}
				else {
					// Validate quantised range
					validate(arg, range);
				}
			}

			case int[] range -> {
				if(range.length != 2) throw new UnsupportedOperationException("Invalid integer range: " + this);
				validate(arg, range[0], range[1]);
			}

			case VulkanBoolean bool -> throw new UnsupportedOperationException("Boolean property cannot be validated: " + this);

			default -> throw new UnsupportedOperationException("Unsupported device limit: " + this);
		}
	}

	/**
	 * Validates a ranged property.
	 */
	private <T extends Number> void validate(T arg, T min, T max) {
		final float f = arg.floatValue();
		if((f < min.floatValue()) || (f > max.floatValue())) {
			throw new IllegalArgumentException(String.format("Property out-of-range: value=%s property=%s", arg, this));
		}
	}

	/**
	 * Validates a quantised property.
	 */
	private void validate(float arg, float[] values) {
		for(float f : values) {
			if(MathsUtil.isEqual(f, arg)) {
				return;
			}
		}
		throw new IllegalArgumentException(String.format("Quantised property out-of-range: value=%f property=%s", arg, this));
	}

	/**
	 * Retrieves this property.
	 * @param <T> Type
	 * @param dev Logical device
	 * @return Property value
	 */
	@SuppressWarnings("unchecked")
	public <T> T get() {
		return (T) value;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof VulkanProperty that) &&
				(this.enabled == that.enabled) &&
				this.key.equals(that.key) &&
				Objects.equals(this.value, that.value);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(key)
				.append("enabled", enabled)
				.append("value", value)
				.build();
	}

	/**
	 * A <i>property key</i> is a descriptor for a compound property.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The property {@link #name} is the field name from the {@link VkPhysicalDeviceLimits} structure</li>
	 * <li>{@link #granularity} is the range step size for a floating-point array type field or {@code null} otherwise, e.g. {@link VkPhysicalDeviceLimits#lineWidthGranularity}</li>
	 * <li>{@link #feature} is the associated device feature name or {@code null} if the property is not optional</li>
	 * <li>{@link #min} is the minimal value for numeric types</li>
	 * </ul>
	 * @see DeviceFeatures
	 */
	public record Key(String name, float min, String granularity, String feature) {
		/**
		 * Constructor.
		 * @param name				Property name
		 * @param min				Minimum value
		 * @param granularity		Optional granularity
		 * @param feature			Optional required device feature
		 */
		public Key {
			Check.notEmpty(name);
		}

		/**
		 * Constructor for a simple property.
		 * @param name Property name
		 */
		public Key(String name) {
			this(name, 0, null, null);
		}

		/**
		 * Constructor for an optional property.
		 * @param name 			Property name
		 * @param feature		Feature name
		 */
		public Key(String name, String feature) {
			this(name, 0, null, feature);
			Check.notEmpty(feature);
		}

		/**
		 * Builder for a property key.
		 */
		public static class Builder {
			private String name;
			private float min;
			private String granularity;
			private String feature;

			/**
			 * Sets the name of this property.
			 * @param name Property name
			 */
			public Builder name(String name) {
				this.name = notEmpty(name);
				return this;
			}

			/**
			 * Sets the minimum value of this property.
			 * @param min Minimum value
			 */
			public Builder min(float min) {
				this.min = min;
				return this;
			}

			/**
			 * Sets the granularity of this ranged property.
			 * @param granularity Granularity field name
			 */
			public Builder granularity(String granularity) {
				this.granularity = notEmpty(granularity);
				return this;
			}

			/**
			 * Sets the required feature for this property.
			 * @param feature Feature name
			 */
			public Builder feature(String feature) {
				this.feature = notEmpty(feature);
				return this;
			}

			/**
			 * Constructs this property key.
			 * @return New property key
			 * @throws IllegalArgumentException if the property name has not been specified
			 */
			public Key build() {
				if(name == null) throw new IllegalArgumentException("Property name not specified");
				return new Key(name, min, granularity, feature);
			}
		}
	}

	/**
	 * The <i>provider</i> generates property values from a {@link Key}.
	 */
	public static class Provider {
		private final VkPhysicalDeviceLimits limits;
		private final Set<String> features;
		private final Map<Key, VulkanProperty> props = new ConcurrentHashMap<>();

		/**
		 * Constructor.
		 * @param limits		Device limits
		 * @param features		Supported features
		 */
		public Provider(VkPhysicalDeviceLimits limits, DeviceFeatures features) {
			this.limits = limits.copy();
			this.features = features.features();
		}

		/**
		 * Retrieves a property.
		 * @param key Property key
		 * @return Property
		 */
		public VulkanProperty property(Key key) {
			return props.computeIfAbsent(key, this::create);
		}

		/**
		 * Retrieves a property by name.
		 * @param name Property name
		 * @return Property
		 */
		public VulkanProperty property(String name) {
			return property(new Key(name));
		}

		/**
		 * Creates a property.
		 * @param key Property key
		 * @return New property
		 */
		private VulkanProperty create(Key key) {
			final boolean enabled = key.feature == null ? true : features.contains(key.feature);
			final Object value = limits.readField(key.name);

			if(key.granularity == null) {
				return new VulkanProperty(key, enabled, value);
			}
			else {
				final float granularity = (float) limits.readField(key.granularity);
				final Object range = range((float[]) value, granularity);
				return new VulkanProperty(key, enabled, range);
			}
		}

		/**
		 * Builds a quantised floating-point range.
		 * @param array				Min/max
		 * @param granularity		Granularity
		 * @return Ranged value
		 */
		private static float[] range(float[] array, float granularity) {
			// Determine range length
			if(array.length != 2) throw new IllegalArgumentException("Expected min/max array");
			final float min = array[0];
			final float max = array[1];
			final int len = (int) ((max - min) / granularity);

			// Build range
			final float[] range = new float[len + 1];
			for(int n = 0; n < len; ++n) {
				range[n] = min + n * granularity;
			}
			range[len] = max;

			return range;
		}
	}
}
