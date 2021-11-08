package org.sarge.jove.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A <i>resource writer</i> defines a mechanism for persisting a resource.
 * <p>
 * A resource writer is comprised of a two-stage approach:
 * <ol>
 * <li>{@link #map(OutputStream)} transforms an output-stream to some intermediate destination type</li>
 * <li>{@link #write(Object, Object)} writes an object to this destination</li>
 * </ol>
 * <p>
 * @param <T> Destination type
 * @param <R> Resource type
 * @see ResourceLoader
 * @see ResourceLoaderWriter
 * @author Sarge
 */
public interface ResourceWriter<OUT, T> {
	/**
	 * Maps the given output-stream to the intermediate destination type.
	 * @param out Output stream
	 * @return Destination
	 * @throws IOException if the stream cannot be mapped
	 */
	OUT map(OutputStream out) throws IOException;

	/**
	 * Writes the given resource.
	 * @param data		Data to write
	 * @param out		Destination
	 * @throws IOException if the data cannot be written
	 */
	void write(T data, OUT out) throws IOException;
}
