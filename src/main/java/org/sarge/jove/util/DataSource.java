package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A <i>data source</i> opens an input-stream for a given resource by name.
 * @author Sarge
 */
public class DataSource {
	/**
	 * Creates a file-system data-source at the given directory.
	 * @param dir Root directory
	 * @return New file-system data-source
	 * @throws IllegalArgumentException if the root directory does not exist
	 */
	public static DataSource of(String dir) {
		final Path root = Paths.get(dir);
		if(!Files.exists(root)) throw new IllegalArgumentException("Data-source directory does not exist: " + root);
		return new DataSource(root);
	}

	private final Path root;

	/**
	 * Constructor.
	 * @param root Root of this data-source
	 */
	public DataSource(Path root) {
		this.root = notNull(root);
	}

	/**
	 * Loads a resource.
	 * @param <T> Data type
	 * @param <R> Resource type
	 * @param name			Resource name
	 * @param loader		Loader
	 * @return Resource
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	public <T, R> R load(String name, ResourceLoader<T, R> loader) {
		try(final InputStream in = Files.newInputStream(root.resolve(name))) {
			final T data = loader.map(in);
			return loader.load(data);
		}
		catch(IOException e) {
			throw new RuntimeException("Error loading resource: " + name, e);
		}
	}

	@Override
	public String toString() {
		return root.toString();
	}
}
