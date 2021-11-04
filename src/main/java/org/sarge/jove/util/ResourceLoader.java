package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * A <i>resource loader</i> defines a mechanism for loading a resource.
 * <p>
 * A resource loader is comprised of a two-stage approach:
 * <ol>
 * <li>{@link #map(InputStream)} transforms an input-stream to some intermediate data type</li>
 * <li>{@link #load(Object)} constructs the resultant object from this data</li>
 * </ol>
 * <p>
 * @param <T> Input type
 * @param <R> Resource type
 * @see DataSource#load(String, ResourceLoader)
 * @see ResourceWriter
 * @see ResourceLoaderWriter
 * @author Sarge
 */
public interface ResourceLoader<T, R> {
	/**
	 * Maps an input stream to the intermediate data type.
	 * @param in Input stream
	 * @return Intermediate data type
	 * @throws IOException if the input data cannot be loaded
	 */
	T map(InputStream in) throws IOException;

	/**
	 * Constructs the resultant resource from the given data.
	 * @param data Input data
	 * @return Loaded resource
	 * @throws IOException if the resource cannot be loaded
	 */
	R load(T data) throws IOException;
}
