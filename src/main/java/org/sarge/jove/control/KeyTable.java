package org.sarge.jove.control;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/**
 * The <i>key table</i> maps GLFW keyboard codes to the corresponding key names.
 * @author Sarge
 */
public class KeyTable {
	/**
	 * TODO - JDK26 lazy construction
	 * TODO - how to specify use specific rather than default? system property?
	 */
	public enum Instance {
		INSTANCE;

		private KeyTable table;

		public synchronized KeyTable get() {
			if(table == null) {
				table = defaultKeyTable();
			}
			return table;
		}

		public void set(KeyTable table) {
			this.table = requireNonNull(table);
		}
	}

	private final Map<String, Button> table;
	private final Map<Integer, Button> index;

	/**
	 * Constructor.
	 * @param keys Key table
	 */
	public KeyTable(List<Button> table) {
		this.table = table.stream().collect(toMap(Button::name, Function.identity()));
		this.index = table.stream().collect(toMap(Button::index, Function.identity()));
	}

	/**
	 * @return Keys indexed by name
	 */
	public Map<String, Button> keys() {
		return table;
	}

	/**
	 * @return Keys indexed by key code
	 */
	public Map<Integer, Button> index() {
		return index;
	}

	/**
	 * Loads the default key definitions specified by the {@code key.table.txt} resource file.
	 * @return Default key table
	 * @see Loader
	 */
	public static KeyTable defaultKeyTable() {
		final var loader = new Loader();
		try(final var in = KeyTable.class.getClassLoader().getResourceAsStream("key.table.txt")) {
			return loader.load(in);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loader for a key table.
	 */
	public static class Loader {
		/**
		 * Loads a key table from the given path.
		 * @return Key table
		 * @throws IOException if the table cannot be loaded
		 */
		public KeyTable load(Path path) throws IOException {
			try(final var in = Files.newInputStream(path)) {
				return load(in);
			}
		}

		/**
		 * Loads a key table from the given input stream.
		 * @param in Input stream
		 * @return Key table
		 */
		public KeyTable load(InputStream in) {
			return new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(String::trim)
					.map(str -> str.split("\\s+"))
					.map(Loader::load)
					.collect(collectingAndThen(toList(), KeyTable::new));
		}

		/**
		 * Loads a key table entry.
		 */
		private static Button load(String[] tokens) {
			final Integer code = Integer.parseInt(tokens[1].trim());
			final String name = tokens[0].trim();
			return new Button(code, name);
		}
	}
}
