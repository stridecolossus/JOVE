package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;

/**
 * The <i>key table</i> maps key codes to key names for a <i>standard</i> keyboard.
 * @author Sarge
 */
public class KeyTable {
	private final BidiMap<Integer, String> table = new DualHashBidiMap<>(load());

	/**
	 * @param key Key code
	 * @return Whether this table contains the given key
	 */
	public boolean contains(int key) {
		return table.containsKey(key);
	}

	/**
	 * Looks up a key name.
	 * @param key Key code
	 * @return Key name
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public String name(int key) {
		final String name = table.get(key);
		if(name == null) throw new IllegalArgumentException("Unknown key: " + key);
		return name;
	}

	/**
	 * @param key Key name
	 * @return Whether this table contains the given key
	 */
	public boolean contains(String key) {
		return table.containsValue(key);
	}

	/**
	 * Looks up a key code by name.
	 * @param name Key name
	 * @return Key code
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public int code(String name) {
		final Integer key = table.getKey(name);
		if(key == null) throw new IllegalArgumentException("Unknown key: " + name);
		return key;
	}

	/**
	 * @return A copy of this key table
	 */
	public Map<Integer, String> map() {
		return Map.copyOf(table);
	}

	/**
	 * Loads the standard key table.
	 */
	private static Map<Integer, String> load() {
		try(final InputStream in = KeyTable.class.getResourceAsStream("/key.table.txt")) {
			if(in == null) throw new RuntimeException("Cannot find key names resource");
			return new BufferedReader(new InputStreamReader(in))
					.lines()
					.map(StringUtils::split)
					.collect(toMap(tokens -> Integer.parseInt(tokens[1].trim()), tokens -> tokens[0].trim()));
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
