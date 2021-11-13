package org.sarge.jove.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A <i>data resource loader</i> defines a resource that can be persisted to/from a data stream.
 * @param <T> Data type
 * @see DataHelper
 * @author Sarge
 */
public interface DataResourceLoader<T> extends ResourceLoader<DataInputStream, T> {
	@Override
	default DataInputStream map(InputStream in) throws IOException {
		return new DataInputStream(in);
	}

	/**
	 * Writes the given data object.
	 * @param data		Data to write
	 * @param out		Output stream
	 * @throws IOException if the data cannot be written
	 */
	void save(T data, DataOutputStream out) throws IOException;
}
