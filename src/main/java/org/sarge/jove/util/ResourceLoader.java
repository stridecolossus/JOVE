package org.sarge.jove.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

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
public interface ResourceLoader<R> {
//	/**
//	 * Maps an input stream to the intermediate data type.
//	 * @param in Input stream
//	 * @return Intermediate data type
//	 * @throws IOException if the input data cannot be loaded
//	 */
//	T map(InputStream in) throws IOException;

	/**
	 * Constructs the resultant resource from the given data.
	 * @param data Input data
	 * @return Loaded resource
	 * @throws IOException if the resource cannot be loaded
	 */
	R load(InputStream in) throws IOException;

	/**
	 *
	 * @param <T>
	 * @param <R>
	 */
	abstract class Adapter<T, R> implements ResourceLoader<R> {
		/**
		 * Maps an input stream to the intermediate data type.
		 * @param in Input stream
		 * @return Intermediate data type
		 * @throws IOException if the input data cannot be loaded
		 */
		protected abstract T map(InputStream in) throws IOException;

		/**
		 *
		 * @param data
		 * @return
		 * @throws IOException
		 */
		protected abstract R load(T data) throws IOException;

		@Override
		public final R load(InputStream in) throws IOException {
			final T data = map(in);
			return load(data);
		}
	}

//	/**
//	 * Helper - Loads a stream of text from the given reader, ignoring empty lines and comments.
//	 * @param r Reader
//	 * @return
//	 * @throws IOException
//	 */
//	static void lines(Reader r, Consumer<Stream<String>> consumer) throws IOException {
//		final LineNumberReader reader = new LineNumberReader(r);
//		try(reader) {
//			final Stream<String> stream = reader
//				.lines()
//				.map(String::trim)
//				.filter(Predicate.not(String::isEmpty))
//				.filter(line -> !line.startsWith("#"));
////				.map(loader);
//
//			consumer.accept(stream);
//		}
//		catch(IOException e) {
//			throw e;
//		}
//		catch(Exception e) {
//			throw new IOException("Error loading at line " + reader.getLineNumber(), e);
//		}
//	}

	/**
	 * Adapter for a resource loader
	 * TODO
	 * @param <T> Intermediate data type
	 * @param <R> Resource type
	 */
	abstract class TextResourceLoader<T, R> extends Adapter<Reader, R> {
		@Override
		protected final Reader map(InputStream in) throws IOException {
			return new InputStreamReader(in);
		}

		/**
		 * Loads the next line.
		 * @param line Next line
		 * @return Data
		 */
		protected abstract T load(String line);

		/**
		 * Returns a collector for the resultant data.
		 * @return Collector
		 */
		protected abstract Collector<T, ?, R> collector();

		@Override
		protected final R load(Reader r) throws IOException {
			final Function<Stream<String>, R> terminal = stream -> stream.map(this::load).collect(collector());
			return lines(r, terminal);
		}
	}

	static <T> T lines(Reader r, Function<Stream<String>, T> terminal) throws IOException {
		final LineNumberReader reader = new LineNumberReader(r);
		try(reader) {
			final Stream<String> lines = reader
				.lines()
				.map(String::trim)
				.filter(Predicate.not(String::isEmpty))
				.filter(line -> !line.startsWith("#"));

			return terminal.apply(lines);
		}
		catch(IOException e) {
			throw e;
		}
		catch(Exception e) {
			throw new IOException("Error loading at line " + reader.getLineNumber(), e);
		}
	}

	// TODO
	// - text loader class
	// - comments strings
	// - lines()

	/**
	 * Creates an adapter for a resource loader based on a data source.
	 * @param <T> Input type
	 * @param <R> Resource type
	 * @param src		Data source
	 * @param loader	Delegate loader
	 * @return Loader adapter
	 */
	static <T, R> Function<String, R> of(DataSource src, ResourceLoader<T, R> loader) {
		return null;
//		return name -> {
//			try(final InputStream in = src.open(name)) {
//				final T data = loader.map(in);
//				return loader.load(data);
//			}
//			catch(IOException e) {
//				throw new RuntimeException("Error loading resource: " + name, e);
//			}
//		};
	}
}
