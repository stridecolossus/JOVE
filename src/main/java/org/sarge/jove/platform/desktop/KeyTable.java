package org.sarge.jove.platform.desktop;

import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.sarge.jove.control.Button;

/**
 * The <i>key table</i> maps key codes to key names or button events for a <i>standard</i> keyboard.
 * @author Sarge
 */
public class KeyTable {
	/**
	 * Singleton instance.
	 */
	public static final KeyTable INSTANCE = new KeyTable();

	private final BidiMap<Integer, String> table = new DualHashBidiMap<>(load());
	private final Map<Integer, Button> buttons = new HashMap<>();

	private KeyTable() {
	}

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
	 * Looks up the keyboard button for the given key-code.
	 * @param code Key-code
	 * @return Keyboard button
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public Button key(int code) {
		return buttons.computeIfAbsent(code, ignored -> new Button(name(code)));
	}

	/**
	 * Looks up a keyboard key by name.
	 * @param name Key name
	 * @return Keyboard button
	 * @throws IllegalArgumentException if the key is unknown
	 */
	public Button key(String name) {
		final int code = code(name);
		return buttons.computeIfAbsent(code, ignored -> new Button(name));
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
