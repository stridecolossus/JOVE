package org.sarge.jove.io;

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
}
