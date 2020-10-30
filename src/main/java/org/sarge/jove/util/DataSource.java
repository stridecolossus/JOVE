package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.sarge.jove.util.Loader.LoaderAdapter;

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

	// TODO - doc

	/**
	 * Creates an adapter for a loader with the given data-source.
	 * @param <R> Resource type
	 * @param <T> Intermediate type
	 * @param src			Data-source
	 * @param loader		Delegate loader
	 * @return Data-source loader
	 */
	static <T, R> Loader<String, R> loader(DataSource src, LoaderAdapter<T, R> loader) {
		return name -> {
			try(final InputStream in = src.open(name)) {
				final T obj = loader.open(in);
				return loader.load(obj);
			}
			catch(IOException e) {
				throw new RuntimeException("Error loading resource: " + name, e);
			}
		};
	}

	/**
	 * Creates an adapter for a loader with the given data-source.
	 * @param <R> Resource type
	 * @param src			Data-source
	 * @param loader		Delegate loader
	 * @return Data-source loader
	 */
	static <R> Loader<String, R> loader(DataSource src, Loader<InputStream, R> loader) {
		return name -> {
			try(final InputStream in = src.open(name)) {
				return loader.load(in);
			}
			catch(IOException e) {
				throw new RuntimeException("Error loading resource: " + name, e);
			}
		};
	}
}

