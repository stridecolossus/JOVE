package org.sarge.jove.io;

import java.io.InputStream;

/**
 * A <i>resource loader</i> loads a resource from an input stream.
 * <p>
 * To allow implementations to operate on a specific data type (or stream) the {@link #map(InputStream)} method is first invoked to transform the input stream to an intermediate data type.
 * <p>
 * For example:
 * <p>
 * <pre>
 * 	ResourceLoader&lt;BufferedReader, String&tg; loader = new ResourceLoader() {
 * 	    public BufferedReader map(InputStream in) throws IOException {
 * 	      return new BufferedReader(new InputStreamReader(in));
 * 	    }
 *
 * 	    public String load(BufferedReader r) throws IOException {
 * 	      return r.readLine();
 * 	    }
 * 	}
 * </pre>
 * This loader would then be used as follows:
 * <pre>
 * 	InputStream in = ...
 * 	BufferedReader r = loader.map(in);
 * 	String line = loader.load(r);
 * </pre>
 * <p>
 * @param <T> Intermediate data type
 * @param <R> Resultant type
 * @see ResourceLoaderAdapter
 * @author Sarge
 */
public interface ResourceLoader<T, R> {
	/**
	 * Maps the given input stream to the intermediate data type for this resource.
	 * @param in Input stream
	 * @return Intermediate data
	 * @throws Exception if the stream cannot be mapped
	 */
	T map(InputStream in) throws Exception;

	/**
	 * Loads this resource.
	 * @param data Input data
	 * @return Resource
	 * @throws Exception if the resource cannot be loaded
	 */
	R load(T data) throws Exception;
}
