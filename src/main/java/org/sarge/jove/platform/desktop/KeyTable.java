package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The <i>key table</i> maps GLFW keyboard codes to the corresponding key names.
 * Key definitions are specified by the {@code key.table.txt} resource file.
 * @author Sarge
 */
class KeyTable {
	private final Map<Integer, String> keys;
	private final Map<String, Integer> codes;

	/**
	 * Constructor.
	 * @throws RuntimeException if the key table cannot be loaded
	 */
	public KeyTable() {
		this.keys = load();
		this.codes = codes(keys);
	}

	/**
	 * Loads the key table.
	 * @return Key table
	 */
	private static Map<Integer, String> load() {
		try(final InputStream in = KeyTable.class.getResourceAsStream("key.table.txt")) {
			return new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(String::trim)
					.map(str -> str.split("\\s+"))
					.map(KeyTable::load)
					.collect(toMap(Entry::getKey, Entry::getValue));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Loads a key table entry.
	 */
	private static Entry<Integer, String> load(String[] tokens) {
		final Integer code = Integer.parseInt(tokens[1].trim());
		final String name = tokens[0].trim();
		return Map.entry(code, name);
	}

	/**
	 * Builds the reverse mapping for key codes.
	 * @param keys Key table
	 * @return Codes table
	 */
	private static Map<String, Integer> codes(Map<Integer, String> keys) {
		return keys
				.entrySet()
				.stream()
				.collect(toMap(Entry::getValue, Entry::getKey));
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
}
