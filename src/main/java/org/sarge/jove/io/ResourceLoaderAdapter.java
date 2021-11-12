package org.sarge.jove.io;

import static org.sarge.lib.util.Check.notNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * A <i>resource loader adapter</i> composes a data source and a resource loader such that resources can be loaded by <i>name</i>.
 * <p>
 * The adapter encapsulates the mapping of the underlying input stream and transforms any checked exceptions.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * 	DataSource src = ...
 * 	ResourceLoader loader = ...
 * 	ResourceLoaderAdapter adapter = new ResourceLoaderAdapter(src, loader);
 * 	Object resource = adapter.load("name");
 * </pre>
 * <p>
 * @param <T> Intermediate type
 * @param <R> Resultant type
 * @author Sarge
 */
public class ResourceLoaderAdapter<T, R> {
	private final DataSource src;
	private final ResourceLoader<T, R> loader;

	/**
	 * Constructor.
	 * @param src			Data source
	 * @param loader		Resource loader
	 */
	public ResourceLoaderAdapter(DataSource src, ResourceLoader<T, R> loader) {
		this.src = notNull(src);
		this.loader = notNull(loader);
	}

	/**
	 * Loads the specified resource.
	 * @param name Resource name
	 * @return Resource
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	public R load(String name) {
		try(final InputStream in = src.input(name)) {
			final T data = loader.map(in);
			return loader.load(data);
		}
		catch(IOException e) {
			throw new RuntimeException("Error loading resource: " + name, e);
		}
	}
}
