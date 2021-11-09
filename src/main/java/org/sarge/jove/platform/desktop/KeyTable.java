package org.sarge.jove.platform.desktop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;

/**
 * The <i>key table</i> maps GLFW keyboard codes to the corresponding key names.
 * @author Sarge
 */
class KeyTable {
	private static final KeyTable INSTANCE = new KeyTable(); // TODO - lazy init?

	/**
	 * @return Singleton key-table instance
	 */
	public static final KeyTable instance() {
		return INSTANCE;
	}

	/**
	 * Loads the key table.
	 * @return Key table
	 */
	private static BidiMap<Integer, String> load() {
		try(final InputStream in = KeyTable.class.getResourceAsStream("/key.table.txt")) {
			return new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(String::trim)
					.map(StringUtils::split)
					.map(KeyTable::load)
					.collect(Collectors.collectingAndThen(Collectors.toMap(Entry::getKey, Entry::getValue), DualHashBidiMap::new));
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

	private final BidiMap<Integer, String> keys = load();

	private KeyTable() {
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
	 */
	public int code(String name) {
		final Integer code = keys.inverseBidiMap().get(name);
		if(code == null) throw new IllegalArgumentException("Unknown key name: " + name);
		return code;
	}
}
