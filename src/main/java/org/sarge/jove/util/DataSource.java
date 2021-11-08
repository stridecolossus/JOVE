package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A <i>data source</i> abstracts over an I/O system by referring to resources by name.
 * @author Sarge
 */
public interface DataSource {
	/**
	 * Opens an input stream for the given resource.
	 * @param name Resource name
	 * @return Input stream
	 * @throws IOException if the resource cannot be opened
	 */
	InputStream input(String name) throws IOException;

	/**
	 * Opens an output stream to the given resource.
	 * @param name Resource name
	 * @return Output stream
	 * @throws IOException if the resource cannot be opened
	 */
	OutputStream output(String name) throws IOException;

	/**
	 * Loads a resource.
	 * @param <T> Data type
	 * @param <R> Resource type
	 * @param name			Resource name
	 * @param loader		Loader
	 * @return Resource
	 * @throws RuntimeException if the resource cannot be loaded
	 */
	default <T, R> R load(String name, ResourceLoader<T, R> loader) {
		try(final InputStream in = input(name)) {
			final T data = loader.map(in);
			return loader.load(data);
		}
		catch(IOException e) {
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
	default <T, R> void write(String name, R data, ResourceWriter<T, R> writer) {
		try(final OutputStream out = output(name)) {
			final T dest = writer.map(out);
			writer.write(data, dest);
		}
		catch(IOException e) {
			throw new RuntimeException("Error writing resource: " + name, e);
		}
	}
}
