package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A <i>data source</i> abstracts over the file-system by referring to resources by name.
 * @author Sarge
 */
public class DataSource {
	/**
	 * Helper - Creates a data-source with a root at the users home directory.
	 * @param path Optional application path
	 * @return New data-source
	 */
	public static DataSource home(Path path) {
		final Path home = Paths.get(System.getProperty("user.home"));
		if(path == null) {
			return new DataSource(home);
		}
		else {
			return new DataSource(home.resolve(path));
		}
	}

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
	 * @return Root of this data-source
	 */
	public Path root() {
		return root;
	}

	/**
	 * Resolves a data-source for the given sub-directory relative to this data-source.
	 * @param path Sub-directory path
	 * @return New sub-directory data-source
	 */
	public DataSource resolve(Path path) {
		return new DataSource(root.resolve(path));
	}

	/**
	 * Resolves the given resource.
	 * @param name Resource name
	 * @return Resource path
	 */
	protected Path resolve(String name) {
		return root.resolve(name);
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
		try(final InputStream in = Files.newInputStream(resolve(name))) {
			final T data = loader.map(in);
			return loader.load(data);
		}
		catch(Exception e) {
			throw new RuntimeException("Error loading resource: " + name, e);
		}
	}

	/**
	 * Writes a resource.
	 * @param <T> Output type
	 * @param <R> Resource type
	 * @param name			Resource name
	 * @param data			Resource
	 * @param writer		Writer
	 * @throws RuntimeException if the resource cannot be persisted
	 */
	public <T, R> void write(String name, R data, ResourceWriter<T, R> writer) {
		try(final OutputStream out = Files.newOutputStream(resolve(name))) {
			final T dest = writer.map(out);
			writer.write(data, dest);
		}
		catch(Exception e) {
			throw new RuntimeException("Error writing resource: " + name, e);
		}
	}

	@Override
	public String toString() {
		return root.toString();
	}
}
