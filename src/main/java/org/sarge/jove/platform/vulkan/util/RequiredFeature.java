package org.sarge.jove.platform.vulkan.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A <i>required feature</i> annotation denotes configuration methods associated with a required device feature.
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
 * <li>numeric values (int, long, float)</li>
 * </ul>
 * <p>
 * The {@link Processor} walks configuration objects (usually Vulkan structure descriptors) via reflection to enumerate required device features.
 * <p>
 * @see DeviceFeatures
 * @author Sarge
 */
@Retention(RUNTIME)
@Target(METHOD)
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
			// Test for required device features
			final Predicate<RequiredFeature> isRequired = annotation -> {
				// Retrieve field
				final Object value;
				try {
					final Field field = obj.getClass().getDeclaredField(annotation.field());
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
					case Number num -> num.floatValue() > annotation.predicate();
					default -> throw new UnsupportedOperationException(String.format("Unsupported required feature: object=%s feature=%s", obj, annotation));
				};
			};

			// Enumerate required features
			final Method[] methods = obj.getClass().getDeclaredMethods();
			return Arrays
					.stream(methods)
					.map(m -> m.getAnnotation(RequiredFeature.class))
					.filter(Objects::nonNull)
					.filter(isRequired)
					.map(RequiredFeature::feature);
		}
	}
}
