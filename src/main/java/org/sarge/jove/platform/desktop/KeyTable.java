package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.*;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * The <i>key table</i> maps GLFW keyboard codes to the corresponding key names.
 * @author Sarge
 */
class KeyTable {
	private final Map<Integer, String> keys;
	private final Map<String, Integer> codes;

	/**
	 * Constructor.
	 */
	public KeyTable(Map<Integer, String> keys) {
		this.keys = Map.copyOf(keys);
		this.codes = codes(keys);
	}

	/**
	 * Builds the inverse mapping for key codes.
	 */
	private static Map<String, Integer> codes(Map<Integer, String> keys) {
		return keys
				.entrySet()
				.stream()
				.collect(toMap(Entry::getValue, Entry::getKey));
	}

	/**
	 * @return Keys indexed by code
	 */
	public Map<Integer, String> keys() {
		return keys;
	}

	/**
	 * Maps the given key code to the corresponding name.
	 * @param key Key code
	 * @return Key name or {@code UNKNOWN} if not present
	 */
	public String name(int key) {
		return keys.getOrDefault(key, "UNKNOWN");
	}

	/**
	 * Maps the given key name to the corresponding code.
	 * @param name Key name
	 * @return Key code or {@code zero} if unknown
	 */
	public int code(String name) {
		return codes.getOrDefault(name, 0);
	}

	/**
	 * Helper.
	 * Maps this key table by the given function.
	 * @param <T> Return type
	 * @param mapper Mapping function
	 * @return Mapped key table
	 */
	public <T> Map<Integer, T> map(BiFunction<Integer, String, T> mapper) {
		return keys
				.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getKey, entry -> mapper.apply(entry.getKey(), entry.getValue())));
	}

	/**
	 * Default key definitions specified by the {@code key.table.txt} resource file.
	 * @return Default key table
	 * @see Loader
	 */
	public static KeyTable defaultKeyTable() {
		final var loader = new Loader();
		try(final var in = KeyTable.class.getResourceAsStream("key.table.txt")) {
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
					.collect(collectingAndThen(toMap(Entry::getKey, Entry::getValue), KeyTable::new));
		}

		/**
		 * Loads a key table entry.
		 */
		private static Entry<Integer, String> load(String[] tokens) {
			final Integer code = Integer.parseInt(tokens[1].trim());
			final String name = tokens[0].trim();
			return Map.entry(code, name);
		}
	}
}
