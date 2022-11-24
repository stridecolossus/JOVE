package org.sarge.jove.platform.vulkan.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * The <i>required feature</i> annotation denotes configuration methods associated with a required device feature.
 * <p>
 * The annotation specifies the following:
 * <ul>
 * <li>a configuration field</li>
 * <li>the corresponding device feature name</li>
 * <li>an optional minimum {@link #predicate} for numeric fields</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * class SomePipelineStage {
 *     private int value;
 *
 *     @RequiredFeature(field="value" feature="feature" predicate=2)
 *     public void setter(int value) {
 *         this.value = value;
 *     }
 * }
 * </pre>
 * <p>
 * The following field types <b>only</b> are supported:
 * <ul>
 * <li>boolean flags</li>
 * <li>numerics: {@code int}, {@code long} or {@code float}</li>
 * </ul>
 * <p>
 * The {@link Processor} walks configuration objects (usually Vulkan structure descriptors) via reflection to enumerate required device features.
 * <p>
 * @see DeviceFeatures
 * @author Sarge
 */
@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(RequiredFeature.List.class)
public @interface RequiredFeature {
	/**
	 * @return Field name
	 */
	String field();

	/**
	 * The <i>predicate</i> specifies a <b>minimum</b> for numeric values to trigger the required device feature.
	 * @return Predicate filter (default is one)
	 */
	float predicate() default 1;

	/**
	 * @return Required feature name
	 */
	String feature();

	/**
	 * Container.
	 */
	@Retention(RUNTIME)
	@Target(METHOD)
	@interface List {
		RequiredFeature[] value();
	}

	/**
	 * The <i>required feature processor</i> enumerates required device features via reflection specified by an annotated configuration object.
	 */
	class Processor {
		/**
		 * Enumerates required device features for the given annotated configuration object.
		 * @param obj Annotated configuration object
		 * @return Required device feature names
		 * @throws RuntimeException for an unknown field
		 * @throws UnsupportedOperationException for an unsupported field type
		 */
		public Stream<String> enumerate(Object obj) {
			final Method[] methods = obj.getClass().getDeclaredMethods();
			return Arrays
					.stream(methods)
					.map(m -> m.getAnnotation(RequiredFeature.class))
					.filter(Objects::nonNull)
					.filter(required -> required(obj, required))
					.map(RequiredFeature::feature);
		}

		/**
		 * Tests whether the a configuration object satisfies the given required feature.
		 * @param obj			Configuration object
		 * @param feature		Required feature
		 * @return Whether required
		 */
		private static boolean required(Object obj, RequiredFeature feature) {
			// Retrieve field
			final Object value;
			try {
				final Field field = obj.getClass().getDeclaredField(feature.field());
				field.setAccessible(true);
				value = field.get(obj);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}

			// Test whether device feature is required
			return switch(value) {
				case null -> false;
				case Boolean b -> b;
				case Number num -> num.floatValue() > feature.predicate();
				default -> throw new UnsupportedOperationException("Unsupported required feature: object=%s feature=%s".formatted(obj, feature));
			};
		}
	}
}
