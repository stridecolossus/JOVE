package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * @param <T> Input data type
 * @param <R> Output resource type
 * @author Sarge
 */
@FunctionalInterface
public interface Loader<T, R> {
	/**
	 * Loads a resource.
	 * @param in Input data
	 * @return Loaded resource
	 */
	R load(T in);

	/**
	 * A <i>data source</i> opens an input-stream for a given resource by name.
	 */
	interface DataSource extends Function<String, InputStream> {
		/**
		 * Creates a file-system data-source at the given directory.
		 * @param dir Directory
		 * @return Data-source
		 * @throws IllegalArgumentException if the directory does not exist
		 */
		static DataSource of(Path dir) {
			if(!Files.exists(dir)) throw new IllegalArgumentException("Data-source directory does not exist: " + dir);

			return filename -> {
				try {
					return Files.newInputStream(dir.resolve(filename));
				}
				catch(IOException e) {
					throw new RuntimeException("Error opening file: " + filename, e);
					// TODO
				}
			};
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
	}

	/**
	 * Creates an adapter for a loader with the given data-source mapping.
	 * @param src			Data-source
	 * @param loader		Delegate loader
	 * @param <R> Resource type
	 * @return Name-mapped loader
	 */
	static <R> Loader<String, R> of(DataSource src, Loader<InputStream, R> loader) {
		return name -> {
			try(final InputStream in = src.apply(name)) {
				return loader.load(in);
			}
			catch(IOException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
