package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * @param <T> Input type
 * @param <R> Resource type
 * @author Sarge
 */
@FunctionalInterface
public interface ResourceLoader<T, R> {
	/**
	 * Loads a resource.
	 * @param in Input data
	 * @return Loaded resource
	 * @throws IOException if the resource cannot be loaded
	 */
	R load(T in) throws IOException;

	/**
	 * Template for a loader used to map an input-stream to its input type.
	 * @param <T> Input type
	 * @param <R> Resource type
	 */
	abstract class Adapter<T, R> implements ResourceLoader<T, R> {
		/**
		 * Maps the given input-stream to the input type for this loader.
		 * @param in Input-stream
		 * @return Input type
		 * @throws IOException if the stream cannot be opened
		 */
		protected abstract T map(InputStream in) throws IOException;
	}

	/**
	 * Creates a loader based on the given data-source.
	 * @param <T> Input type
	 * @param <R> Resource type
	 * @param src			Data-source
	 * @param loader		Loader
	 * @return Resource loader
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	static <T, R> ResourceLoader<String, R> of(DataSource src, Adapter<T, R> loader) {
		return name -> {
			try(final InputStream in = src.open(name)) {
				final T input = loader.map(in);
				return loader.load(input);
			}
			catch(IOException e) {
				throw new RuntimeException("Error loading resource: " + name, e);
			}
		};
	}
}
