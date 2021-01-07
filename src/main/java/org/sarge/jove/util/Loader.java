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
public interface Loader<T, R> {
	/**
	 * Loads a resource.
	 * @param in Input data
	 * @return Loaded resource
	 * @throws IOException if the resource cannot be loaded
	 */
	R load(T in) throws IOException;

	/**
	 * Adapter for a loader used to map an input-stream to its input type.
	 * @param <T> Input type
	 * @param <R> Resource type
	 */
	abstract class Adapter<T, R> implements Loader<T, R> {
		/**
		 * Maps the given input-stream to the input type for this loader.
		 * @param in Input-stream
		 * @return Input type
		 * @throws IOException if the stream cannot be opened
		 */
		protected abstract T map(InputStream in) throws IOException;
	}
}
