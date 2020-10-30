package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * @param <R> Resource type
 * @author Sarge
 */
@FunctionalInterface
public interface Loader<T, R> {
	/**
	 * Loads a resource.
	 * @param in Input data
	 * @return Loaded resource
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	R load(T in);

	/**
	 * Loader based on an input-stream.
	 * @param <R> Resource type
	 */
	interface InputStreamLoader<R> extends Loader<InputStream, R> {
		// Marker interface
	}

	/**
	 * Adapter for a loader with an intermediate data type mapped from an {@link InputStream}.
	 * <p>
	 * Note that the {@link #load(InputStream)} method wraps any IO exceptions as a {@link RuntimeException}.
	 * <p>
	 * Example for a loader implemented using a byte-array stream:
	 * <pre>
	 *  class ByteArrayStreamLoader extends LoaderAdapter<ByteArrayInputStream, Thing> {
	 *      @Override
	 *      protected ByteArrayInputStream(InputStream in) throws IOException {
	 *          return new ByteArrayInputStream(in);
	 *      }
	 *
	 *      @Override
	 *      protected Thing load(ByteArrayInputStream in) {
	 *      	...
	 *      }
	 *  }
	 * </pre>
	 * <p>
	 * @param <T> Intermediate type
	 * @param <R> Resource type
	 */
	abstract class LoaderAdapter<T, R> implements InputStreamLoader<R> {
		@Override
		public final R load(InputStream in) {
			try {
				return create(open(in));
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Maps the given input-stream to an instance of the intermediate type.
		 * @param in Input-stream
		 * @return Intermediate object
		 * @throws IOException if the stream cannot be opened
		 */
		protected abstract T open(InputStream in) throws IOException;

		/**
		 * Creates the output resource.
		 * @param obj Intermediate instance
		 * @return Loaded resource
		 * @throws IOException if the resource cannot be loaded
		 */
		protected abstract R create(T obj) throws IOException;
	}
}
