package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A <i>text loader</i> is a helper class that loads the lines from a file and delegates to a loader function.
 * <p>
 * Example:
 * <p>
 * <pre>
 * 	Function<Stream<String>, Integer> mapper = stream -> stream.mapToInt(Integer::parseInt).sum();
 * 	TextLoader loader = new TextLoader();
 * 	int total = loader.load(in, mapper);
 * </pre>
 * <p>
 * @author Sarge
 */
public class TextLoader {
	private Set<String> comments = Set.of("#");
	private int skip;

	/**
	 * Tests whether a given line is a comment.
	 * @param line Line
	 * @return Whether the given line starts with a comment token
	 * @see #setComments(Set)
	 */
	protected boolean isComment(String line) {
		return comments.stream().anyMatch(line::startsWith);
	}

	/**
	 * Sets the comment string(s).
	 * @param comments Comment strings
	 */
	public void setComments(Set<String> comments) {
		this.comments = Set.copyOf(comments);
	}

	/**
	 * Sets the number of header lines to skip.
	 * @param skip Number of lines to skip (default is none)
	 */
	public void setSkipHeaderLines(int skip) {
		this.skip = zeroOrMore(skip);
	}

	/**
	 * Loads text from a reader and delegates to the given loader function.
	 * @param <T> Resultant data type
	 * @param r				Reader
	 * @param loader		Delegate loader
	 * @return Results
	 * @throws IOException if the text cannot be loaded
	 */
	public <T> T load(Reader r, Function<Stream<String>, T> loader) throws IOException {
		final LineNumberReader reader = new LineNumberReader(r);
		try(reader) {
			// Load lines
			final Stream<String> lines = reader
					.lines()
					.skip(skip)
					.map(String::trim)
					.filter(Predicate.not(String::isEmpty))
					.filter(Predicate.not(this::isComment));

			// Delegate
			return loader.apply(lines);
		}
		catch(IOException e) {
			throw e;
		}
		catch(Exception e) {
			throw new IOException(String.format("%s at line %d", e.getMessage(), reader.getLineNumber() + 1), e);
		}
	}

	// load(r, Consumer<String>) => Function<Stream<String>, Void>
	// List<T> load(r, Function<String, T> mapper)
	// R load(r, Function<String, T> mapper, Collector<T, ?, R> collector) => as below

	/**
	 * Adapter for a resource loader based on a {@link TextLoader}.
	 * <p>
	 * Usage:
	 * <p>
	 * <pre>
	 * 	TextResourceLoader loader = new TextResourceLoader() {
	 * 		protected String load(String line) {
	 * 			return line;
	 * 		}
	 *
	 * 		protected Collector<String, ?, List<String>> collector() {
	 * 			return Collectors.toList();
	 * 		}
	 * 	};
	 * </pre>
	 */
	public static abstract class TextResourceLoader<T, R> implements ResourceLoader<Reader, R> {
		private TextLoader loader = new TextLoader();

		/**
		 * Sets the underlying text loader.
		 * @param loader Text loader
		 */
		public void loader(TextLoader loader) {
			this.loader = notNull(loader);
		}

		@Override
		public Reader map(InputStream in) throws IOException {
			return new InputStreamReader(in);
		}

		@Override
		public R load(Reader reader) throws IOException {
			final Function<Stream<String>, R> mapper = stream -> stream.map(this::load).collect(collector());
			return loader.load(reader, mapper);
		}

		/**
		 * Loads and parses line.
		 * @param line Line
		 * @return Data
		 */
		protected abstract T load(String line);

		/**
		 * @return Data collector
		 */
		protected abstract Collector<T, ?, R> collector();
	}
}
