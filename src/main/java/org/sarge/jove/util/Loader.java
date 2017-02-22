package org.sarge.jove.util;

import java.io.IOException;

/**
 * Defines a resource loader.
 * @author Sarge
 * @param <T> Resource type
 */
@FunctionalInterface
public interface Loader<T> {
	/**
	 * Loads a resource from the given path.
	 * @param path Resource path
	 * @return Resource
	 * @throws IOException if the resource cannot be loaded
	 */
	T load(String path) throws IOException;
}
