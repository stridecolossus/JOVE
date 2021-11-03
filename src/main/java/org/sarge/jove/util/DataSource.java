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
	private final Path root;

	/**
	 * Constructor.
	 * @param root Data-source root path
	 * @throws IllegalArgumentException if the root does not exist
	 */
	public DataSource(Path root) {
		if(!Files.exists(root)) throw new IllegalArgumentException("Data-source root does not exist: " + root);
		this.root = notNull(root);
	}

	/**
	 * Constructor.
	 * @param root Data-source root
	 * @throws IllegalArgumentException if the root does not exist
	 */
	public DataSource(String root) {
		this(Paths.get(root));
	}

	/**
	 * @return Data-source root path
	 */
	public Path root() {
		return root;
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
	// TODO - add default method for mapping filename -> stream, rename map() to transform()

	@Override
	public String toString() {
		return root.toString();
	}
}
