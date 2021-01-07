package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A <i>data source</i> opens an input-stream for a given resource by name.
 */
public interface DataSource {
	/**
	 * Opens the resource with the given name.
	 * @param name Resource name
	 * @return Input-stream
	 * @throws IOException if the resource cannot be opened
	 */
	InputStream open(String name) throws IOException;

	/**
	 * Creates a file-system data-source at the given directory.
	 * @param dir Directory
	 * @return Data-source
	 * @throws IllegalArgumentException if the directory does not exist
	 */
	static DataSource of(Path dir) {
		if(!Files.exists(dir)) throw new IllegalArgumentException("Data-source directory does not exist: " + dir);
		return name -> Files.newInputStream(dir.resolve(name));
	}

	/**
	 * Creates a file-system data-source at the given directory.
	 * @param dir Directory
	 * @return Data-source
	 * @throws IllegalArgumentException if the directory does not exist
	 */
	static DataSource of(String dir) {
		return of(Paths.get(dir));
	}

	/**
	 * Creates a loader based on this data-source.
	 * @param <T> Input type
	 * @param <R> Resource type
	 * @param loader Loader adapter
	 * @return Data-source loader
	 */
	default <T, R> Loader<String, R> loader(Loader.Adapter<T, R> loader) {
		return name -> {
			try(final InputStream in = open(name)) {
				final T input = loader.map(in);
				return loader.load(input);
			}
			catch(IOException e) {
				throw new RuntimeException("Error loading resource: " + name, e);
			}
		};
	}
}
