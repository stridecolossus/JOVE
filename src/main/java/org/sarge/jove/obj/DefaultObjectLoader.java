package org.sarge.jove.obj;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.sarge.lib.util.Check;

/**
 * Base-class loader for OBJ data.
 * @author Sarge
 * @param <T> Data type
 */
class DefaultObjectLoader<T> {
	private static final Predicate<String> EMPTY = Predicate.not(String::isEmpty);
	private static final Predicate<String> COMMENTS = Predicate.not(line -> line.startsWith("#"));

	/**
	 * Command parser.
	 * @param <T> Data type
	 */
	protected static interface Parser<T> {
		/**
		 * Parses a command line.
		 * @param tokens		Command and arguments
		 * @param model			Mutable data model
		 */
		void parse(String[] tokens, T model);

		/**
		 * @return Minimum number of arguments
		 */
		default int min() {
			return 1;
		}

		/**
		 * @return Maximum number of arguments
		 */
		default int max() {
			return min();
		}
	}

	/**
	 * Base-class for an array-based parser.
	 * @param <T> Model type
	 * @param <V> Data type
	 */
	protected static class ArrayParser<V, T> implements Parser<T> {
		private final int len;
		private final Function<float[], V> ctor;
		private final BiConsumer<V, T> setter;

		/**
		 * Constructor.
		 * @param len		Maximum array length
		 * @param ctor		Constructor
		 * @param setter	Model setter
		 */
		protected ArrayParser(int len, Function<float[], V> ctor, BiConsumer<V, T> setter) {
			this.len = oneOrMore(len);
			this.ctor = notNull(ctor);
			this.setter = notNull(setter);
		}

		@Override
		public void parse(String[] tokens, T model) {
			// Convert to array
			final float[] array = new float[len];
			for(int n = 0; n < tokens.length - 1; ++n) {
				array[n] = Float.parseFloat(tokens[n + 1]);
			}

			// TODO
//			// Populate trailing default values
//			final int max = max();
//			for(int n = tokens.length - 1; n < max; ++n) {
//				array[n] = def();
//			}

			// Construct value
			final V value = ctor.apply(array);

			// Add to model
			setter.accept(value, model);
		}

		@Override
		public final int min() {
			return len;
		}

		/**
		 * @return Default value
		 */
		protected int def() {
			return 1;
		}
	}

	private final Map<String, Parser<T>> parsers = new HashMap<>();

	private boolean ignore;

	/**
	 * Registers a parser.
	 * @param cmd	Command
	 * @param p		Parser
	 * @throws IllegalArgumentException if the parser min/max arguments are invalid
	 */
	public void add(String cmd, Parser<T> p) {
		Check.notEmpty(cmd);
		Check.notNull(p);
		if(p.max() < p.min()) throw new IllegalArgumentException("Invalid min/max number of arguments");
		parsers.put(cmd, p);
	}

	/**
	 * Unsupported commands are ignored rather than throwing an exception.
	 */
	public void ignoreUnsupportedCommands() {
		ignore = true;
	}

	/**
	 * Loads from the given reader and delegates commands to the registered parsers.
	 * @param r 		Reader
	 * @param model		Mutable data model
	 * @throws IOException if the data cannot be loaded
	 * @throws UnsupportedOperationException for an unsupported command unless {@link #ignoreUnsupportedCommands()} has been invoked on this loader
	 */
	protected void load(Reader r, T model) throws IOException {
		try(final LineNumberReader in = new LineNumberReader(r)) {
			try {
				in.lines()
					.map(String::trim)
					.filter(EMPTY)
					.filter(COMMENTS)
					.forEach(line -> parse(line, model));
			}
			catch(Exception e) {
				throw new IOException(String.format("%s at line %d", e.getMessage(), in.getLineNumber()), e);
			}
		}
	}

	/**
	 * Parses a command line.
	 * @param line		Command line
	 * @param model		Data model
	 */
	private void parse(String line, T model) {
		// Tokenize command and arguments
		final String[] tokens = StringUtils.split(line);

		// Lookup parser for next command
		final Parser<T> p = parsers.get(tokens[0]);
		if(p == null) {
			if(ignore) {
				return;
			}
			else {
				throw new UnsupportedOperationException("Unsupported OBJ command: " + tokens[0]);
			}
		}

		// Verify number of arguments
		final int num = tokens.length - 1;
		if((num < p.min()) || (num > p.max())) {
			throw new IllegalArgumentException(String.format("Invalid number of command arguments: command=%s actual=%d expected=%d-%d", tokens[0], num, p.min(), p.max()));
		}

		// Delegate to parser
		p.parse(tokens, model);
	}
}
