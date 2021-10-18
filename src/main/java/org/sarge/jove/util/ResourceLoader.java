package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * A <i>loader</i> defines a mechanism for loading a resource.
 * <p>
 * A resource loader is comprised of a two-stage approach:
 * <ol>
 * <li>{@link #map(InputStream)} transforms an input-stream to some intermediate data type</li>
 * <li>{@link #load(Object)} constructs the resultant object from this data</li>
 * </ol>
 * <p>
 * For example a text-based loader could be implemented as follows:
 * <pre>
 * 	class SomeLoader implements ResourceLoader<Reader, String> {
 * 		public Reader map(InputStream in) throws IOException {
 * 			return new InputStreamReader(in);
 * 		}
 *
 * 		public String load(Reader r) throws IOException {
 * 			return r.readLine();
 * 		}
 * 	}
 * </pre>
 * <p>
 * The {@link #of(DataSource, ResourceLoader)} factory is used to combine a resource loader with a data source:
 * <pre>
 * 	DataSource src = ...
 * 	var loader = ResourceLoader.of(src, new SomeLoader());
 * 	String result = loader.apply("filename");
 * </pre>
 * <p>
 * @param <T> Input type
 * @param <R> Resource type
 * @author Sarge
 */
public interface ResourceLoader<T, R> {
	/**
	 * Maps an input stream to the intermediate data type.
	 * @param in Input stream
	 * @return Intermediate data type
	 * @throws IOException if the input data cannot be loaded
	 */
	T map(InputStream in) throws IOException;

	/**
	 * Constructs the resultant resource from the given data.
	 * @param data Input data
	 * @return Loaded resource
	 * @throws IOException if the resource cannot be loaded
	 */
	R load(T data) throws IOException;

	/**
	 * Creates an adapter for a resource loader based on a data source.
	 * @param <T> Input type
	 * @param <R> Resource type
	 * @param src		Data source
	 * @param loader	Delegate loader
	 * @return Loader adapter
	 */
	static <T, R> Function<String, R> of(DataSource src, ResourceLoader<T, R> loader) {
		return name -> {
			try(final InputStream in = src.open(name)) {
				final T data = loader.map(in);
				return loader.load(data);
			}
			catch(IOException e) {
				throw new RuntimeException("Error loading resource: " + name, e);
			}
		};
	}
}
