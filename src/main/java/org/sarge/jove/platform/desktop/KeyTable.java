package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * The <i>key table</i> maps GLFW keyboard codes to the corresponding key names.
 * @author Sarge
 */
enum KeyTable {
	INSTANCE;

	private final Map<Integer, String> keys;
	private final Map<String, Integer> codes;

	private KeyTable() {
		this.keys = load();
		this.codes = keys.keySet().stream().collect(toMap(keys::get, Function.identity()));
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
	 * Looks up a keyboard key name.
	 * @param key GLFW key code
	 * @return Key name or {@code UNKNOWN} if not present
	 */
	public String name(int key) {
		return keys.getOrDefault(key, "UNKNOWN");
	}

	/**
	 * Looks up the key code for the given keyboard key name.
	 * @param name Key name
	 * @return GLFW key code
	 * @throws IllegalArgumentException for an unknown key
	 */
	public int code(String name) {
		final Integer code = codes.get(name);
		if(code == null) throw new IllegalArgumentException("Unknown key name: " + name);
		return code;
	}
}
