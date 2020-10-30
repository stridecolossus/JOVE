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
	 * Adapter for a loader with an intermediate data type mapped from an {@link InputStream}.
	 * @param <T> Intermediate type
	 * @param <R> Resource type
	 */
	abstract class LoaderAdapter<T, R> implements Loader<T, R> {
		/**
		 * Maps the given input-stream to an instance of the intermediate type.
		 * @param in Input-stream
		 * @return Intermediate object
		 * @throws IOException if the stream cannot be opened
		 */
		protected abstract T open(InputStream in) throws IOException;
	}
}
